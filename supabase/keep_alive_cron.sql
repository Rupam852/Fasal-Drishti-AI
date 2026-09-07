-- ============================================================
-- Fasal Drishti — Supabase Anti-Pause (Keep-Alive) Cron Job
-- ============================================================

-- 1. Enable pg_cron extension (built into Supabase Postgres)
create extension if not exists pg_cron;

-- 2. Remove any existing heartbeat job to avoid duplicates
select cron.unschedule('fasal-drishti-keep-alive');

-- 3. Schedule Keep-Alive Heartbeat every day at 00:00 UTC
-- This updates a timestamp in app_config, keeping the database active
select cron.schedule(
  'fasal-drishti-keep-alive',
  '0 0 * * *',
  $$
    update public.app_config
    set updated_at = timezone('utc'::text, now())
    where key = 'latest_app_version';
  $$
);

-- Check scheduled cron jobs
select jobid, jobname, schedule, command, active from cron.job;
