package com.ruoyi.web.core.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures the RuoYi code generator metadata tables exist in partially imported
 * development databases.
 */
@Component
public class GenTableSchemaInitializer implements CommandLineRunner
{
    private final JdbcTemplate jdbcTemplate;

    public GenTableSchemaInitializer(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args)
    {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS gen_table (
              table_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
              table_name varchar(200) DEFAULT '' COMMENT 'Table name',
              table_comment varchar(500) DEFAULT '' COMMENT 'Table comment',
              sub_table_name varchar(64) DEFAULT NULL COMMENT 'Sub table name',
              sub_table_fk_name varchar(64) DEFAULT NULL COMMENT 'Sub table foreign key',
              class_name varchar(100) DEFAULT '' COMMENT 'Entity class name',
              tpl_category varchar(200) DEFAULT 'crud' COMMENT 'Template category',
              tpl_web_type varchar(30) DEFAULT '' COMMENT 'Frontend template type',
              package_name varchar(100) DEFAULT NULL COMMENT 'Package name',
              module_name varchar(30) DEFAULT NULL COMMENT 'Module name',
              business_name varchar(30) DEFAULT NULL COMMENT 'Business name',
              function_name varchar(50) DEFAULT NULL COMMENT 'Function name',
              function_author varchar(50) DEFAULT NULL COMMENT 'Function author',
              form_col_num int(1) DEFAULT 1 COMMENT 'Form column count',
              gen_type char(1) DEFAULT '0' COMMENT 'Generate type',
              gen_path varchar(200) DEFAULT '/' COMMENT 'Generate path',
              options varchar(1000) DEFAULT NULL COMMENT 'Options',
              create_by varchar(64) DEFAULT '' COMMENT 'Create by',
              create_time datetime DEFAULT NULL COMMENT 'Create time',
              update_by varchar(64) DEFAULT '' COMMENT 'Update by',
              update_time datetime DEFAULT NULL COMMENT 'Update time',
              remark varchar(500) DEFAULT NULL COMMENT 'Remark',
              PRIMARY KEY (table_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Code generator table metadata'
            """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS gen_table_column (
              column_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
              table_id bigint(20) DEFAULT NULL COMMENT 'Table ID',
              column_name varchar(200) DEFAULT NULL COMMENT 'Column name',
              column_comment varchar(500) DEFAULT NULL COMMENT 'Column comment',
              column_type varchar(100) DEFAULT NULL COMMENT 'Column type',
              java_type varchar(500) DEFAULT NULL COMMENT 'Java type',
              java_field varchar(200) DEFAULT NULL COMMENT 'Java field',
              is_pk char(1) DEFAULT NULL COMMENT 'Primary key flag',
              is_increment char(1) DEFAULT NULL COMMENT 'Auto increment flag',
              is_required char(1) DEFAULT NULL COMMENT 'Required flag',
              is_insert char(1) DEFAULT NULL COMMENT 'Insert flag',
              is_edit char(1) DEFAULT NULL COMMENT 'Edit flag',
              is_list char(1) DEFAULT NULL COMMENT 'List flag',
              is_query char(1) DEFAULT NULL COMMENT 'Query flag',
              query_type varchar(200) DEFAULT 'EQ' COMMENT 'Query type',
              html_type varchar(200) DEFAULT NULL COMMENT 'HTML type',
              dict_type varchar(200) DEFAULT '' COMMENT 'Dict type',
              sort int(11) DEFAULT NULL COMMENT 'Sort',
              create_by varchar(64) DEFAULT '' COMMENT 'Create by',
              create_time datetime DEFAULT NULL COMMENT 'Create time',
              update_by varchar(64) DEFAULT '' COMMENT 'Update by',
              update_time datetime DEFAULT NULL COMMENT 'Update time',
              PRIMARY KEY (column_id),
              KEY idx_gen_table_column_table_id (table_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Code generator column metadata'
            """);
    }
}
