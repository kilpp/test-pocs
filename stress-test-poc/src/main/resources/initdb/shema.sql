create table if not exists DUNGEON_GAME(
    id uuid not null primary key default gen_random_uuid(),
    minimal_health integer not null,
    execution_time timestamptz not null
);