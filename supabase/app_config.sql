-- ============================================================
-- Fasal Drishti — Dynamic App Config & Remote API Keys
-- ============================================================

-- Table to store dynamic API keys, feature flags, and update configs
create table if not exists public.app_config (
  key text primary key,
  value text not null,
  description text,
  updated_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- Enable RLS
alter table public.app_config enable row level security;

-- Allow read access
create policy "Allow read access to app_config"
  on public.app_config
  for select
  using (true);

-- Seed NVIDIA NIM API Key into Supabase
insert into public.app_config (key, value, description)
values
  ('nvidia_nim_api_key', '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', 'NVIDIA NIM API Key for AI Agronomist Chat'),
  ('latest_app_version', '1.0.0', 'Latest available app version for updater'),
  ('app_download_url', 'https://github.com/Rupam852/Fasal-Drishti-AI/releases/latest', 'Direct APK download link')
on conflict (key) do update set
  value = excluded.value,
  updated_at = now();
