create table if not exists notice (
    id bigint auto_increment primary key,
    source varchar(30) not null,
    external_id varchar(255) not null,
    title varchar(500) not null,
    content longtext,
    summary longtext,
    organized_content longtext,
    category varchar(100),
    display_date varchar(255),
    published_at timestamp,
    original_url varchar(1000) not null,
    ai_generated boolean not null,
    collected_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_notice_source_external_id unique (source, external_id)
);

create table if not exists scheduled_job_lock (
    lock_name varchar(100) primary key,
    locked_until timestamp not null,
    locked_at timestamp not null,
    locked_by varchar(200) not null
);
