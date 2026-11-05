-- Tweede migratie: spelerstatistieken, trainingsresultaten en wedstrijden (voetbalmanager-systeem).
--
-- Hoe het werkt:
--  * Een trainer legt oefeningen vast, bijvoorbeeld: spits, afwerken, 100 schoten, 100 raak  (drill_results).
--  * Uit de oefeningen rekenen we per speler een stat van 1 tot 20 uit (view player_stats). 20 is het best.
--  * Uit de stats rekenen we per positie een totaalcijfer van 1 tot 99 uit, zoals een FIFA-kaartje (view player_cards).
--  * Wedstrijden staan er ook in (matches en match_players). Voor nu met vaste testdata.
-- De database blijft klein: 2 opzoektabellen, 3 gewone tabellen en 2 views.

-- ===== Opzoektabellen (vaste gegevens) =====

-- De acht stats. "drill" legt uit wat de oefening meet: pogingen en hoeveel er raak/gelukt zijn.
create table public.stat_types (
  code text primary key,
  label text not null,
  drill text not null
);

insert into public.stat_types (code, label, drill) values
  ('FINISHING',   'Afwerken',   'Schoten op doel: aantal schoten en aantal goals'),
  ('PASSING',     'Passen',     'Passes naar een medespeler: aantal passes en aantal aangekomen'),
  ('DRIBBLING',   'Dribbelen',  'Dribbels langs een pion: aantal pogingen en aantal gelukt'),
  ('DEFENDING',   'Verdedigen', 'Duels: aantal duels en aantal gewonnen'),
  ('PACE',        'Snelheid',   'Sprints: aantal sprints en aantal binnen de tijd'),
  ('STAMINA',     'Conditie',   'Intervallopen: aantal rondes en aantal binnen de tijd'),
  ('STRENGTH',    'Kracht',     'Lichaamsduels: aantal duels en aantal gewonnen'),
  ('GOALKEEPING', 'Keepen',     'Schoten op de keeper: aantal schoten en aantal gestopt');

-- Hoe zwaar elke stat meetelt voor het totaalcijfer van een positie (per positie samen 1.00).
create table public.position_weights (
  position text not null check (position in ('KEEPER', 'DEFENCE', 'MIDFIELD', 'ATTACK')),
  stat text not null references public.stat_types (code),
  weight numeric(3, 2) not null check (weight > 0 and weight <= 1),
  primary key (position, stat)
);

insert into public.position_weights (position, stat, weight) values
  ('KEEPER',   'GOALKEEPING', 0.60), ('KEEPER',   'PASSING',   0.10), ('KEEPER',   'STRENGTH', 0.10),
  ('KEEPER',   'DEFENDING',   0.10), ('KEEPER',   'PACE',      0.05), ('KEEPER',   'STAMINA',  0.05),
  ('DEFENCE',  'DEFENDING',   0.35), ('DEFENCE',  'STRENGTH',  0.20), ('DEFENCE',  'PACE',     0.15),
  ('DEFENCE',  'PASSING',     0.15), ('DEFENCE',  'STAMINA',   0.15),
  ('MIDFIELD', 'PASSING',     0.30), ('MIDFIELD', 'DRIBBLING', 0.20), ('MIDFIELD', 'STAMINA',  0.20),
  ('MIDFIELD', 'DEFENDING',   0.10), ('MIDFIELD', 'FINISHING', 0.10), ('MIDFIELD', 'PACE',     0.10),
  ('ATTACK',   'FINISHING',   0.35), ('ATTACK',   'PACE',      0.20), ('ATTACK',   'DRIBBLING', 0.20),
  ('ATTACK',   'STRENGTH',    0.10), ('ATTACK',   'PASSING',   0.10), ('ATTACK',   'STAMINA',   0.05);

-- ===== Gewone tabellen =====

-- Eén resultaat van een oefening: een speler deed {attempts} pogingen en {successes} lukten.
create table public.drill_results (
  id bigint generated always as identity primary key,
  player_id bigint not null references public.players (id) on delete cascade,
  stat text not null references public.stat_types (code),
  drill text not null,
  attempts int not null check (attempts > 0),
  successes int not null check (successes >= 0),
  recorded_on date not null default current_date,
  check (successes <= attempts)
);

create table public.matches (
  id bigint generated always as identity primary key,
  team_id bigint not null references public.teams (id) on delete cascade,
  opponent text not null,
  played_on date not null,
  home boolean not null default true,
  goals_for int not null default 0 check (goals_for >= 0),
  goals_against int not null default 0 check (goals_against >= 0)
);

-- Wat een speler in een wedstrijd deed.
create table public.match_players (
  match_id bigint not null references public.matches (id) on delete cascade,
  player_id bigint not null references public.players (id) on delete cascade,
  minutes int not null default 0 check (minutes between 0 and 120),
  goals int not null default 0 check (goals >= 0),
  assists int not null default 0 check (assists >= 0),
  primary key (match_id, player_id)
);

create index drill_results_player_idx on public.drill_results (player_id);
create index matches_team_idx on public.matches (team_id);
create index match_players_player_idx on public.match_players (player_id);

-- ===== Views: de berekende cijfers =====
-- security_invoker: de view volgt de beveiliging van de tabellen eronder. Een trainer ziet dus alleen zijn eigen spelers.

-- Stat 1 tot 20 per speler en per stat: het slagingspercentage over alle oefeningen.
-- 0% geeft 1, 100% geeft 20. Een stat zonder oefeningen heeft geen rij.
create view public.player_stats with (security_invoker = true) as
select
  d.player_id,
  d.stat,
  greatest(1, least(20, round(1 + 19 * sum(d.successes)::numeric / sum(d.attempts))))::int as rating,
  count(*)::int as sessions
from public.drill_results d
group by d.player_id, d.stat;

-- Totaalcijfer 1 tot 99 per speler, volgens de gewichten van zijn positie (zoals een FIFA-kaartje).
-- Ontbreekt een stat nog, dan tellen we alleen de stats die er wel zijn. Zonder oefeningen is het cijfer leeg (null).
create view public.player_cards with (security_invoker = true) as
select
  p.id as player_id,
  p.team_id,
  p.name,
  p.position,
  case
    when count(s.rating) = 0 then null
    else greatest(1, least(99, round(
      sum(w.weight * s.rating) / sum(w.weight) filter (where s.rating is not null) / 20.0 * 99
    )))::int
  end as overall
from public.players p
join public.position_weights w on w.position = p.position
left join public.player_stats s on s.player_id = p.id and s.stat = w.stat
group by p.id, p.team_id, p.name, p.position;

-- ===== Beveiliging =====

alter table public.stat_types enable row level security;
alter table public.position_weights enable row level security;
alter table public.drill_results enable row level security;
alter table public.matches enable row level security;
alter table public.match_players enable row level security;

create policy "ingelogde gebruikers lezen de stats-lijst"
  on public.stat_types for select to authenticated using (true);

create policy "ingelogde gebruikers lezen de gewichten"
  on public.position_weights for select to authenticated using (true);

create policy "trainer beheert oefeningen van eigen spelers"
  on public.drill_results for all to authenticated
  using (exists (
    select 1 from public.players p join public.teams t on t.id = p.team_id
    where p.id = drill_results.player_id and t.owner_id = (select auth.uid())))
  with check (exists (
    select 1 from public.players p join public.teams t on t.id = p.team_id
    where p.id = drill_results.player_id and t.owner_id = (select auth.uid())));

create policy "trainer beheert wedstrijden van eigen team"
  on public.matches for all to authenticated
  using (team_id in (select id from public.teams where owner_id = (select auth.uid())))
  with check (team_id in (select id from public.teams where owner_id = (select auth.uid())));

create policy "trainer beheert wedstrijdspelers van eigen team"
  on public.match_players for all to authenticated
  using (exists (
    select 1 from public.matches m join public.teams t on t.id = m.team_id
    where m.id = match_players.match_id and t.owner_id = (select auth.uid())))
  with check (exists (
    select 1 from public.matches m join public.teams t on t.id = m.team_id
    where m.id = match_players.match_id and t.owner_id = (select auth.uid())));

-- Niet ingelogde bezoekers (anon) komen niet bij de views.
revoke all on public.player_stats, public.player_cards from anon;

-- ===== Demo-team met vaste testdata =====
-- Maakt het demo-team (zoals eerst), en vult dat met oefeningen en drie wedstrijden.
-- De cijfers zijn "vast": ze worden uit de naam van de speler berekend, dus elke keer hetzelfde.
-- Zo zie je in de app meteen verschil tussen sterke en zwakke spelers (en dus de kleuren).
create or replace function public.create_demo_team() returns bigint
language plpgsql
as $$
declare
  v_team_id bigint;
begin
  v_team_id := public.save_team(
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
