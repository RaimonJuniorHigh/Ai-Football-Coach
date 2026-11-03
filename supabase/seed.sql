-- Testdata die bij `supabase db reset` (lokaal) automatisch wordt gevuld, en online met `supabase db push --include-seed`.
-- Dit is één hele club: 18 spelers met de juiste posities (2 keepers, 6 verdedigers, 6 middenvelders, 4 aanvallers).
-- Daarmee kan een trainer een geldig team samenstellen (minimaal 13 spelers, 1 keeper, 3 verdedigers, 3 middenvelders, 2 aanvallers).
-- Alle namen zijn verzonnen. Het script kan vaker draaien zonder dubbele spelers (on conflict do nothing).
-- Het demo-team zelf staat niet hier, maar wordt per gebruiker gemaakt met de functie create_demo_team().

insert into public.club_players (name, position) values
  -- Keepers (2)
  ('Milan Brouwer',  'KEEPER'),
  ('Jorrit Hulsman', 'KEEPER'),
  -- Verdedigers (6)
  ('Noah Peeters',   'DEFENCE'),
  ('Finn Smeets',    'DEFENCE'),
  ('Ruben Goossens', 'DEFENCE'),
  ('Daniël Aerts',   'DEFENCE'),
  ('Mick Hermans',   'DEFENCE'),
  ('Sven Wouters',   'DEFENCE'),
  -- Middenvelders (6)
  ('Luuk Jacobs',    'MIDFIELD'),
  ('Thijs Maes',     'MIDFIELD'),
  ('Gijs Verbeek',   'MIDFIELD'),
  ('Niels Kramer',   'MIDFIELD'),
  ('Joep van Rooij', 'MIDFIELD'),
  ('Tijn Beekman',   'MIDFIELD'),
  -- Aanvallers (4)
  ('Bram Claes',     'ATTACK'),
  ('Mees Willems',   'ATTACK'),
  ('Cas Dijkstra',   'ATTACK'),
  ('Lenn Schouten',  'ATTACK')
on conflict (name) do nothing;
