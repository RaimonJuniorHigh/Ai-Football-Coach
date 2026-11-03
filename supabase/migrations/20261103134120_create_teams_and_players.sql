-- Eerste migratie: teams, spelers en de clublijst, plus twee functies.
-- De velden komen overeen met de modellen in de Android-app (Team, Player, ClubPlayer).

-- ===== Tabellen =====

create table public.teams (
  id bigint generated always as identity primary key,
  name text not null,
  -- Trainingsdagen als lijst, bijvoorbeeld {TUE,THU}.
  training_days text[] not null default '{}'
    check (training_days <@ array['MON','TUE','WED','THU','FRI']),
  session_minutes int not null default 90 check (session_minutes in (60, 75, 90)),
  invite_code text not null unique,
  -- De trainer die dit team beheert. Wordt automatisch de ingelogde gebruiker.
  owner_id uuid not null default auth.uid() references auth.users (id) on delete cascade,
  created_at timestamptz not null default now()
);

create table public.players (
  id bigint generated always as identity primary key,
  team_id bigint not null references public.teams (id) on delete cascade,
  name text not null,
  position text not null check (position in ('KEEPER', 'DEFENCE', 'MIDFIELD', 'ATTACK')),
  attendance text not null default 'PRESENT' check (attendance in ('PRESENT', 'ABSENT', 'INJURED')),
  created_at timestamptz not null default now()
);

-- Fictieve clublijst waaruit de trainer spelers kan importeren (voor iedereen hetzelfde, alleen lezen).
create table public.club_players (
  id bigint generated always as identity primary key,
  name text not null unique,
  position text not null check (position in ('KEEPER', 'DEFENCE', 'MIDFIELD', 'ATTACK'))
);

create index players_team_id_idx on public.players (team_id);

-- ===== Beveiliging (Row Level Security) =====
-- Zonder deze regels kan iedereen met de publieke sleutel alles lezen en wijzigen.

alter table public.teams enable row level security;
alter table public.players enable row level security;
alter table public.club_players enable row level security;

create policy "trainer beheert eigen team"
  on public.teams for all
  to authenticated
  using (owner_id = (select auth.uid()))
  with check (owner_id = (select auth.uid()));

create policy "trainer beheert spelers van eigen team"
  on public.players for all
  to authenticated
  using (team_id in (select id from public.teams where owner_id = (select auth.uid())))
  with check (team_id in (select id from public.teams where owner_id = (select auth.uid())));

create policy "ingelogde gebruikers lezen de clublijst"
  on public.club_players for select
  to authenticated
  using (true);

-- ===== Functies =====

-- Slaat een team met spelers op en vervangt het oude team van deze trainer.
-- Alles lukt of niets: functies draaien in één transactie.
-- p_players is een lijst zoals [{"name": "Daan", "position": "KEEPER", "attendance": "PRESENT"}].
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
  delete from public.teams where owner_id = (select auth.uid());

  insert into public.teams (name, training_days, session_minutes, invite_code)
  values (p_name, p_training_days, p_session_minutes, p_invite_code)
  returning id into v_team_id;

  insert into public.players (team_id, name, position, attendance)
  select v_team_id,
         p ->> 'name',
         p ->> 'position',
         coalesce(p ->> 'attendance', 'PRESENT')
  from jsonb_array_elements(p_players) as p;

  return v_team_id;
end;
$$;

-- Maakt een demo-team (JO17-1 met 16 verzonnen spelers) voor de ingelogde gebruiker.
-- Zo staat de testdata in de database en niet in de app.
create function public.create_demo_team() returns bigint
language sql
as $$
  select public.save_team(
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
$$;

-- Alleen ingelogde gebruikers (ook anonieme) mogen de functies aanroepen.
revoke execute on function public.save_team(text, text[], int, text, jsonb) from public, anon;
revoke execute on function public.create_demo_team() from public, anon;
grant execute on function public.save_team(text, text[], int, text, jsonb) to authenticated;
grant execute on function public.create_demo_team() to authenticated;
