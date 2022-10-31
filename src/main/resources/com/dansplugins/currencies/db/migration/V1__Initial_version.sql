create table `currencies_currency`(
    `id` varchar(36) primary key not null,
    `version` integer not null,
    `faction_id` varchar(36) not null,
    `name` varchar(256) not null unique,
    `description` varchar(4096) not null,
    `item` blob not null,
    `amount` integer not null,
    `status` varchar(8) not null,
    `legacy_id` integer null
);

create table `currencies_balance`(
    `player_id` varchar(36) not null,
    `currency_id` varchar(36) not null,
    `version` integer not null,
    `balance` integer not null,
    primary key(`player_id`, `currency_id`)
);