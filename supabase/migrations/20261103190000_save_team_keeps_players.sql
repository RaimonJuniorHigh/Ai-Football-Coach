-- Derde migratie: opnieuw opslaan van het team mag geen spelers en resultaten meer weggooien.
-- De app opent bij elke start het scherm "je team" en slaat dat bij "Volgende" op. Met de oude save_team
-- (die het team verwijderde en opnieuw aanmaakte) verdwenen daardoor alle spelers, oefeningen en wedstrijden.
-- Nu past save_team het bestaande team aan (naam, trainingsdagen, duur) en laat spelers en resultaten staan.
-- De oude "vervangen"-versie blijft bestaan onder de naam replace_team, voor create_demo_team.

alter function public.save_team(text, text[], int, text, jsonb) rename to replace_team;

create function public.save_team(
  p_name text,
  p_training_days text[],
  p_session_minutes int,
  p_invite_code text,
  p_players jsonb
) returns bigint
language plpgsql
as $$
declare
  v_team_id bigint;
begin
  select id into v_team_id
  from public.teams
  where owner_id = (select auth.uid())
  order by id desc
  limit 1;

  if v_team_id is null then
    -- Eerste keer: team en spelers aanmaken.
    insert into public.teams (name, training_days, session_minutes, invite_code)
    values (p_name, p_training_days, p_session_minutes, p_invite_code)
    returning id into v_team_id;

    insert into public.players (team_id, name, position, attendance)
    select v_team_id,
           p ->> 'name',
           p ->> 'position',
           coalesce(p ->> 'attendance', 'PRESENT')
    from jsonb_array_elements(p_players) as p;
  else
    -- Bestaand team: alleen de teamgegevens aanpassen. De uitnodigingscode blijft hetzelfde,
    -- zodat de link die al gedeeld is blijft werken.
    update public.teams
    set name = p_name, training_days = p_training_days, session_minutes = p_session_minutes
    where id = v_team_id;
  end if;

  return v_team_id;
end;
$$;

revoke execute on function public.save_team(text, text[], int, text, jsonb) from public, anon;
grant execute on function public.save_team(text, text[], int, text, jsonb) to authenticated;

-- Het demo-team vervangt een bestaand team, dus het gebruikt replace_team.
create or replace function public.create_demo_team() returns bigint
language plpgsql
as $$
declare
  v_team_id bigint;
begin
  v_team_id := public.replace_team(
    'JO17-1',
    array['TUE', 'THU'],
    90,
    substr(md5(random()::text), 1, 4),
    '[
      {"name": "Daan Verhoeven", "position": "KEEPER"},
      {"name": "Tim Hendriks",   "position": "KEEPER"},
      {"name": "Sem Vossen",     "position": "DEFENCE"},
      {"name": "Jayden Bakker",  "position": "DEFENCE"},
      {"name": "Stijn Mulder",   "position": "DEFENCE"},
      {"name": "Lucas van Dijk", "position": "DEFENCE"},
      {"name": "Thomas Visser",  "position": "DEFENCE"},
      {"name": "Jesse Daniëls",  "position": "MIDFIELD"},
      {"name": "Koen Dekker",    "position": "MIDFIELD"},
      {"name": "Max Hoekstra",   "position": "MIDFIELD"},
      {"name": "Olivier Smit",   "position": "MIDFIELD"},
      {"name": "Julian Meijer",  "position": "MIDFIELD"},
      {"name": "Lars de Wit",    "position": "ATTACK"},
      {"name": "Rick Bos",       "position": "ATTACK"},
      {"name": "Tygo Kok",       "position": "ATTACK"},
      {"name": "Dylan Vink",     "position": "ATTACK"}
    ]'::jsonb
  );

  -- Twee trainingsrondes per speler en per stat die bij zijn positie hoort, elk met 100 pogingen.
  -- Kans = 35% + gewicht van de stat + talent van de speler (-20% tot +20%) + toeval per stat (-10% tot +10%),
  -- en de tweede ronde is 4% beter (de speler wordt beter).
  insert into public.drill_results (player_id, stat, drill, attempts, successes, recorded_on)
  select
    p.id,
    w.stat,
    t.drill,
    100,
    round(100 * least(1.0, greatest(0.05,
      0.35 + w.weight
      + ((abs(hashtext(p.name)) % 41) - 20) / 100.0
      + ((abs(hashtext(p.name || w.stat)) % 21) - 10) / 100.0
      + (r.round_no - 1) * 0.04)))::int,
    current_date - (14 - 7 * (r.round_no - 1))
  from public.players p
  join public.position_weights w on w.position = p.position
  join public.stat_types t on t.code = w.stat
  cross join generate_series(1, 2) as r (round_no)
  where p.team_id = v_team_id;

  -- Drie wedstrijden.
  insert into public.matches (team_id, opponent, played_on, home, goals_against) values
    (v_team_id, 'SV Sint-Joris', current_date - 21, true,  1),
    (v_team_id, 'VV Eendracht',  current_date - 14, false, 0),
    (v_team_id, 'Rapid Boys',    current_date -  7, true,  2);

  -- Wie speelde wat: ongeveer 70% speelt 90 minuten, 20% een helft en de rest zit erbuiten.
  -- Aanvallers scoren het vaakst, middenvelders geven de meeste assists.
  insert into public.match_players (match_id, player_id, minutes, goals, assists)
  select
    m.id,
    p.id,
    case when abs(hashtext(p.name || m.opponent)) % 100 < 70 then 90 else 45 end,
    case p.position
      when 'ATTACK'   then case when abs(hashtext(m.opponent || p.name)) % 100 < 40 then 1 else 0 end
      when 'MIDFIELD' then case when abs(hashtext(m.opponent || p.name)) % 5 = 0 then 1 else 0 end
      else 0
    end,
    case p.position
      when 'MIDFIELD' then abs(hashtext(p.name || m.opponent || 'a')) % 2
      when 'ATTACK'   then case when abs(hashtext(p.name || m.opponent || 'a')) % 4 = 0 then 1 else 0 end
      else 0
    end
  from public.matches m
  join public.players p on p.team_id = m.team_id
  where m.team_id = v_team_id
    and abs(hashtext(p.name || m.opponent || 'x')) % 100 < 85;

  -- De einduitslag klopt met de doelpunten van de spelers.
  update public.matches m
  set goals_for = coalesce((select sum(mp.goals) from public.match_players mp where mp.match_id = m.id), 0)
  where m.team_id = v_team_id;

  return v_team_id;
end;
$$;
