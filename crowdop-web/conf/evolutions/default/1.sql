# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table assignment (
  id                        bigint not null,
  assigned_task_id          bigint,
  worker_id                 varchar(255),
  assignment_id             varchar(255),
  accept_date_time          timestamp,
  submit_date_time          timestamp,
  result                    varchar(255),
  freetext                  varchar(255),
  constraint pk_assignment primary key (id))
;

create table data_source (
  id                        bigint not null,
  name                      varchar(255),
  html_template             varchar(255),
  type                      varchar(255),
  owner_email               varchar(255),
  constraint pk_data_source primary key (id))
;

create table job (
  id                        bigint not null,
  folder                    varchar(255),
  name                      varchar(255),
  op_json_str               varchar(255),
  price                     double,
  state                     varchar(255),
  tmp                       varchar(255),
  assign_num                integer,
  assigned_project_id       bigint,
  output_source_id          bigint,
  constraint pk_job primary key (id))
;

create table project (
  id                        bigint not null,
  name                      varchar(255),
  query_stat                varchar(255),
  budget                    double,
  state                     integer,
  owner_email               varchar(255),
  dest_data_source_id       bigint,
  price_cselect_base        double,
  price_cselect_inc         double,
  price_cjoin_base          double,
  price_cjoin_inc           double,
  price_cfill_base          double,
  price_cfill_inc           double,
  constraint pk_project primary key (id))
;

create table crowdtask (
  id                        bigint not null,
  folder                    varchar(255),
  token                     varchar(255),
  multi_label               boolean,
  htmls                     TEXT,
  options                   TEXT,
  assigned_job_id           bigint,
  constraint pk_crowdtask primary key (id))
;

create table task_option (
  id                        bigint not null,
  name                      varchar(255),
  text                      varchar(255),
  constraint pk_task_option primary key (id))
;

create table account (
  email                     varchar(255) not null,
  name                      varchar(255),
  password                  varchar(255),
  constraint pk_account primary key (email))
;

create sequence assignment_seq;

create sequence data_source_seq;

create sequence job_seq;

create sequence project_seq;

create sequence crowdtask_seq;

create sequence task_option_seq;

create sequence account_seq;

alter table assignment add constraint fk_assignment_assignedTask_1 foreign key (assigned_task_id) references crowdtask (id) on delete restrict on update restrict;
create index ix_assignment_assignedTask_1 on assignment (assigned_task_id);
alter table data_source add constraint fk_data_source_owner_2 foreign key (owner_email) references account (email) on delete restrict on update restrict;
create index ix_data_source_owner_2 on data_source (owner_email);
alter table job add constraint fk_job_assignedProject_3 foreign key (assigned_project_id) references project (id) on delete restrict on update restrict;
create index ix_job_assignedProject_3 on job (assigned_project_id);
alter table job add constraint fk_job_outputSource_4 foreign key (output_source_id) references data_source (id) on delete restrict on update restrict;
create index ix_job_outputSource_4 on job (output_source_id);
alter table project add constraint fk_project_owner_5 foreign key (owner_email) references account (email) on delete restrict on update restrict;
create index ix_project_owner_5 on project (owner_email);
alter table project add constraint fk_project_destDataSource_6 foreign key (dest_data_source_id) references data_source (id) on delete restrict on update restrict;
create index ix_project_destDataSource_6 on project (dest_data_source_id);
alter table crowdtask add constraint fk_crowdtask_assignedJob_7 foreign key (assigned_job_id) references job (id) on delete restrict on update restrict;
create index ix_crowdtask_assignedJob_7 on crowdtask (assigned_job_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists assignment;

drop table if exists data_source;

drop table if exists job;

drop table if exists project;

drop table if exists crowdtask;

drop table if exists task_option;

drop table if exists account;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists assignment_seq;

drop sequence if exists data_source_seq;

drop sequence if exists job_seq;

drop sequence if exists project_seq;

drop sequence if exists crowdtask_seq;

drop sequence if exists task_option_seq;

drop sequence if exists account_seq;

