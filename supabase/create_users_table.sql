-- ============================================================
-- Fasal Drishti — Supabase Users & Profiles Table Setup
-- ============================================================
-- Execute this SQL in your Supabase Project -> SQL Editor
-- This ensures every user login automatically saves to both:
-- 1. Table Editor -> 'users'
-- 2. Table Editor -> 'profiles'
-- ============================================================

-- 1. Create public.users table
create table if not exists public.users (
  id text primary key,
  name text not null default 'Farmer',
  email text,
  avatar_url text,
  total_scans integer default 0,
  healthy_count integer default 0,
  diseased_count integer default 0,
  created_at timestamp with time zone default timezone('utc'::text, now()) not null,
  updated_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 2. Create public.profiles table (for compatibility)
create table if not exists public.profiles (
  id text primary key,
  name text not null default 'Farmer',
  email text,
  avatar_url text,
  total_scans integer default 0,
  created_at timestamp with time zone default timezone('utc'::text, now()) not null,
  updated_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 3. Enable Row Level Security (RLS)
alter table public.users enable row level security;
alter table public.profiles enable row level security;

-- 4. RLS Policies for public.users
drop policy if exists "Allow public read access to users" on public.users;
create policy "Allow public read access to users"
  on public.users
  for select
  using (true);

drop policy if exists "Allow insert/upsert to users" on public.users;
create policy "Allow insert/upsert to users"
  on public.users
  for insert
  with check (true);

drop policy if exists "Allow update to users" on public.users;
create policy "Allow update to users"
  on public.users
  for update
  using (true);

-- 5. RLS Policies for public.profiles
drop policy if exists "Allow public read access to profiles" on public.profiles;
create policy "Allow public read access to profiles"
  on public.profiles
  for select
  using (true);

drop policy if exists "Allow insert/upsert to profiles" on public.profiles;
create policy "Allow insert/upsert to profiles"
  on public.profiles
  for insert
  with check (true);

drop policy if exists "Allow update to profiles" on public.profiles;
create policy "Allow update to profiles"
  on public.profiles
  for update
  using (true);

-- 6. Grant access permissions to anon and authenticated roles
grant usage on schema public to anon, authenticated;
grant all on table public.users to anon, authenticated;
grant all on table public.profiles to anon, authenticated;

-- 7. (Optional) Auto-Trigger from Supabase Auth (auth.users -> public.users)
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer set search_path = public
as $$
begin
  insert into public.users (id, name, email, avatar_url, updated_at)
  values (
    new.id::text,
    coalesce(new.raw_user_meta_data->>'full_name', new.raw_user_meta_data->>'name', 'Farmer'),
    new.email,
    new.raw_user_meta_data->>'avatar_url',
    now()
  )
  on conflict (id) do update set
    name = excluded.name,
    email = excluded.email,
    avatar_url = excluded.avatar_url,
    updated_at = now();

  insert into public.profiles (id, name, email, avatar_url, updated_at)
  values (
    new.id::text,
    coalesce(new.raw_user_meta_data->>'full_name', new.raw_user_meta_data->>'name', 'Farmer'),
    new.email,
    new.raw_user_meta_data->>'avatar_url',
    now()
  )
  on conflict (id) do update set
    name = excluded.name,
    email = excluded.email,
    avatar_url = excluded.avatar_url,
    updated_at = now();

  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute procedure public.handle_new_user();
