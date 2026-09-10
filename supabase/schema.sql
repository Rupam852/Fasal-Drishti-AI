-- ============================================================
-- Fasal Drishti — Supabase Database & Storage Schema
-- ============================================================

-- 1. Dynamic App Configuration Table (for Remote API Keys like NVIDIA NIM)
create table if not exists public.app_config (
  key text primary key,
  value text not null,
  description text,
  updated_at timestamp with time zone default timezone('utc'::text, now()) not null
);

alter table public.app_config enable row level security;

drop policy if exists "Allow read access to app_config" on public.app_config;
create policy "Allow read access to app_config"
  on public.app_config
  for select
  using (true);

-- Initial seed for NVIDIA NIM API Key & Updater configs
insert into public.app_config (key, value, description)
values
  ('nvidia_nim_api_key', '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', 'NVIDIA NIM API Key for AI Agronomist Chat'),
  ('nvidia_model_name', 'meta/llama-3.2-11b-vision-instruct', 'Best active Vision & Agronomy Model on NVIDIA NIM'),
  ('latest_app_version', '1.0.0', 'Latest available app version for updater'),
  ('app_download_url', 'https://github.com/Rupam852/Fasal-Drishti-AI/releases/latest', 'Direct APK download link')
on conflict (key) do update set
  value = excluded.value,
-- 2. Users Table (Stores authenticated farmer profiles)
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

alter table public.users enable row level security;

drop policy if exists "Allow public read access to users" on public.users;
create policy "Allow public read access to users"
  on public.users for select using (true);

drop policy if exists "Allow insert/upsert to users" on public.users;
create policy "Allow insert/upsert to users"
  on public.users for insert with check (true);

drop policy if exists "Allow update to users" on public.users;
create policy "Allow update to users"
  on public.users for update using (true);

-- 3. Profiles Table (Compatibility view)
create table if not exists public.profiles (
  id text primary key,
  name text not null default 'Farmer',
  email text,
  avatar_url text,
  total_scans integer default 0,
  created_at timestamp with time zone default timezone('utc'::text, now()) not null,
  updated_at timestamp with time zone default timezone('utc'::text, now()) not null
);

alter table public.profiles enable row level security;

drop policy if exists "Allow public read access to profiles" on public.profiles;
create policy "Allow public read access to profiles"
  on public.profiles for select using (true);

drop policy if exists "Allow insert/upsert to profiles" on public.profiles;
create policy "Allow insert/upsert to profiles"
  on public.profiles for insert with check (true);

drop policy if exists "Allow update to profiles" on public.profiles;
create policy "Allow update to profiles"
  on public.profiles for update using (true);

-- 4. Scans Table (Stores all user disease scan records)
create table if not exists public.scans (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references auth.users(id) on delete cascade,
  image_url text not null,
  predicted_class text not null,
  confidence numeric(5, 4) not null,
  crop_name text not null,
  disease_name text not null,
  severity text default 'Moderate',
  symptoms text,
  treatment text,
  is_synced boolean default true,
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 3. Disease Info Table (Static knowledge library of 38 classes)
create table if not exists public.disease_info (
  class_id text primary key,
  crop_name text not null,
  crop_hindi text,
  disease_name text not null,
  disease_hindi text,
  severity text not null default 'Moderate',
  is_healthy boolean not null default false,
  symptoms text not null,
  symptoms_hindi text,
  treatment text not null,
  treatment_hindi text,
  prevention text not null,
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 4. Row Level Security (RLS) for Scans
alter table public.scans enable row level security;

drop policy if exists "Users can view their own scans" on public.scans;
create policy "Users can view their own scans"
  on public.scans
  for select
  using (true);

drop policy if exists "Users can insert their own scans" on public.scans;
create policy "Users can insert their own scans"
  on public.scans
  for insert
  with check (true);

drop policy if exists "Users can update their own scans" on public.scans;
create policy "Users can update their own scans"
  on public.scans
  for update
  using (true);

drop policy if exists "Users can delete their own scans" on public.scans;
create policy "Users can delete their own scans"
  on public.scans
  for delete
  using (true);

-- 5. RLS for Disease Info (Public Read-Only)
alter table public.disease_info enable row level security;

drop policy if exists "Allow public read access to disease info" on public.disease_info;
create policy "Allow public read access to disease info"
  on public.disease_info
  for select
  using (true);

-- 6. Storage Bucket Configuration (for Crop Scan Images)
insert into storage.buckets (id, name, public)
values ('crop-scans', 'crop-scans', true)
on conflict (id) do nothing;

drop policy if exists "Public can view crop scans" on storage.objects;
create policy "Public can view crop scans"
  on storage.objects
  for select
  to public
  using (bucket_id = 'crop-scans');

drop policy if exists "Authenticated users can upload crop scans" on storage.objects;
create policy "Authenticated users can upload crop scans"
  on storage.objects
  for insert
  to public
  with check (bucket_id = 'crop-scans');
