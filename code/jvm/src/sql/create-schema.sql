create schema dbo;

create table if not exists dbo.User(
    id serial primary key,
    name varchar(64) unique not null,
    password_hash varchar(60) not null,
    email varchar(64) unique not null check (email ~ '^[A-Za-z0-9+_.-]+@(.+)$'),
    rating int default 600 check (rating >= 0),
    games_played int default 0 check (games_played >= 0),
    wins int default 0 check (wins >= 0),
    draws int default 0 check (draws >= 0),
    losses int default 0 check (losses >= 0),
    check (wins + draws + losses = games_played)
);

create table if not exists dbo.Game(
    id serial primary key,
    board jsonb not null,
    black_player int references dbo.User(id) on delete cascade,
    white_player int references dbo.User(id) on delete cascade,
    turn char(1) default 'B' not null check(turn in ('W', 'B')), -- White, Black
    state char(1) default 'R' not null check(state in ('R', 'W', 'B', 'D')), -- Running, WhiteWin, BlackWin, Draw
    config jsonb not null
);

create table if not exists dbo.Token(
    token_hash varchar(256) primary key,
    user_id int references dbo.User(id) on delete cascade,
    created_at bigint not null,
    last_used_at bigint not null
);

create table if not exists dbo.Lobby(
    user_id int references dbo.User(id) on delete cascade,
    config jsonb not null
);
