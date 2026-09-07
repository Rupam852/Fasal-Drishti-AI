-- ============================================================
-- Fasal Drishti — Supabase Database & Storage Schema
-- ============================================================

-- 1. Scans Table (Stores all user disease scan records)
create table if not exists public.scans (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references auth.users(id) on delete cascade not null,
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

-- 2. Disease Info Table (Static knowledge library of 38 classes)
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

-- 3. Row Level Security (RLS) for Scans
alter table public.scans enable row level security;

create policy "Users can view their own scans"
  on public.scans
  for select
  using (auth.uid() = user_id);

create policy "Users can insert their own scans"
  on public.scans
  for insert
  with check (auth.uid() = user_id);

create policy "Users can update their own scans"
  on public.scans
  for update
  using (auth.uid() = user_id);

create policy "Users can delete their own scans"
  on public.scans
  for delete
  using (auth.uid() = user_id);

-- 4. RLS for Disease Info (Public Read-Only)
alter table public.disease_info enable row level security;

create policy "Allow public read access to disease info"
  on public.disease_info
  for select
  using (true);

-- 5. Storage Bucket Configuration (for Crop Scan Images)
-- Note: Run in Supabase SQL editor or create 'crop-scans' bucket in Storage dashboard
insert into storage.buckets (id, name, public)
values ('crop-scans', 'crop-scans', true)
on conflict (id) do nothing;

create policy "Authenticated users can upload crop scans"
  on storage.objects
  for insert
  to authenticated
  with check (bucket_id = 'crop-scans' and (storage.foldername(name))[1] = auth.uid()::text);

create policy "Public can view crop scans"
  on storage.objects
  for select
  to public
  using (bucket_id = 'crop-scans');
