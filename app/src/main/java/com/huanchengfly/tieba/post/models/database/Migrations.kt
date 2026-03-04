package com.huanchengfly.tieba.post.models.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from LitePal v37 to Room v38.
 *
 * LitePal creates all columns as nullable, but Room requires NOT NULL for Kotlin
 * non-null types. SQLite doesn't support ALTER COLUMN, so we use the table rebuild
 * pattern: create new table -> copy data (COALESCE NULLs) -> drop old -> rename.
 *
 * Also creates unique indices that Room expects but LitePal didn't create,
 * after deduplicating entries that would violate the constraints.
 */
val MIGRATION_37_38 = object : Migration(37, 38) {
    override fun migrate(db: SupportSQLiteDatabase) {
        rebuildAccount(db)
        rebuildBlock(db)
        rebuildDraft(db)
        rebuildHistory(db)
        rebuildSearchHistory(db)
        rebuildSearchPostHistory(db)
        rebuildTopForum(db)
    }

    private fun rebuildAccount(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `account_new` (
                `uid` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `bduss` TEXT NOT NULL,
                `tbs` TEXT NOT NULL,
                `portrait` TEXT NOT NULL,
                `stoken` TEXT NOT NULL,
                `cookie` TEXT NOT NULL,
                `nameshow` TEXT,
                `intro` TEXT,
                `sex` TEXT,
                `fansnum` TEXT,
                `postnum` TEXT,
                `threadnum` TEXT,
                `concernnum` TEXT,
                `tbage` TEXT,
                `age` TEXT,
                `birthdayshowstatus` TEXT,
                `birthdaytime` TEXT,
                `constellation` TEXT,
                `tiebauid` TEXT,
                `loadsuccess` INTEGER NOT NULL,
                `uuid` TEXT,
                `zid` TEXT,
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
            )
        """)
        db.execSQL("""
            INSERT INTO `account_new`
                (`uid`,`name`,`bduss`,`tbs`,`portrait`,`stoken`,`cookie`,
                 `nameshow`,`intro`,`sex`,`fansnum`,`postnum`,`threadnum`,
                 `concernnum`,`tbage`,`age`,`birthdayshowstatus`,`birthdaytime`,
                 `constellation`,`tiebauid`,`loadsuccess`,`uuid`,`zid`,`id`)
            SELECT
                COALESCE(`uid`,''), COALESCE(`name`,''), COALESCE(`bduss`,''),
                COALESCE(`tbs`,''), COALESCE(`portrait`,''), COALESCE(`stoken`,''),
                COALESCE(`cookie`,''), `nameshow`, `intro`, `sex`, `fansnum`,
                `postnum`, `threadnum`, `concernnum`, `tbage`, `age`,
                `birthdayshowstatus`, `birthdaytime`, `constellation`, `tiebauid`,
                COALESCE(`loadsuccess`,0), `uuid`, `zid`, `id`
            FROM `account`
        """)
        db.execSQL("DROP TABLE `account`")
        db.execSQL("ALTER TABLE `account_new` RENAME TO `account`")
    }

    private fun rebuildBlock(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `block_new` (
                `category` INTEGER NOT NULL,
                `type` INTEGER NOT NULL,
                `keywords` TEXT,
                `username` TEXT,
                `uid` TEXT,
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
            )
        """)
        db.execSQL("""
            INSERT INTO `block_new` (`category`,`type`,`keywords`,`username`,`uid`,`id`)
            SELECT COALESCE(`category`,0), COALESCE(`type`,0),
                   `keywords`, `username`, `uid`, `id`
            FROM `block`
        """)
        db.execSQL("DROP TABLE `block`")
        db.execSQL("ALTER TABLE `block_new` RENAME TO `block`")
    }

    private fun rebuildDraft(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `draft_new` (
                `hash` TEXT NOT NULL,
                `content` TEXT NOT NULL,
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
            )
        """)
        db.execSQL("""
            INSERT INTO `draft_new` (`hash`,`content`,`id`)
            SELECT COALESCE(`hash`,''), COALESCE(`content`,''), `id`
            FROM `draft`
        """)
        db.execSQL("DROP TABLE `draft`")
        db.execSQL("ALTER TABLE `draft_new` RENAME TO `draft`")
        db.execSQL("""
            DELETE FROM `draft` WHERE `id` NOT IN (
                SELECT MAX(`id`) FROM `draft` GROUP BY `hash`
            )
        """)
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_draft_hash` ON `draft` (`hash`)")
    }

    private fun rebuildHistory(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `history_new` (
                `title` TEXT NOT NULL,
                `data` TEXT NOT NULL,
                `type` INTEGER NOT NULL,
                `timestamp` INTEGER NOT NULL,
                `count` INTEGER NOT NULL,
                `extras` TEXT,
                `avatar` TEXT,
                `username` TEXT,
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
            )
        """)
        db.execSQL("""
            INSERT INTO `history_new`
                (`title`,`data`,`type`,`timestamp`,`count`,`extras`,`avatar`,`username`,`id`)
            SELECT
                COALESCE(`title`,''), COALESCE(`data`,''), COALESCE(`type`,0),
                COALESCE(`timestamp`,0), COALESCE(`count`,0),
                `extras`, `avatar`, `username`, `id`
            FROM `history`
        """)
        db.execSQL("DROP TABLE `history`")
        db.execSQL("ALTER TABLE `history_new` RENAME TO `history`")
    }

    private fun rebuildSearchHistory(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `searchhistory_new` (
                `content` TEXT NOT NULL,
                `timestamp` INTEGER NOT NULL,
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
            )
        """)
        db.execSQL("""
            INSERT INTO `searchhistory_new` (`content`,`timestamp`,`id`)
            SELECT COALESCE(`content`,''), COALESCE(`timestamp`,0), `id`
            FROM `searchhistory`
        """)
        db.execSQL("DROP TABLE `searchhistory`")
        db.execSQL("ALTER TABLE `searchhistory_new` RENAME TO `searchhistory`")
        db.execSQL("""
            DELETE FROM `searchhistory` WHERE `id` NOT IN (
                SELECT MAX(`id`) FROM `searchhistory` GROUP BY `content`
            )
        """)
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_searchhistory_content` ON `searchhistory` (`content`)")
    }

    private fun rebuildSearchPostHistory(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `searchposthistory_new` (
                `content` TEXT NOT NULL,
                `forumname` TEXT NOT NULL,
                `timestamp` INTEGER NOT NULL,
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
            )
        """)
        db.execSQL("""
            INSERT INTO `searchposthistory_new` (`content`,`forumname`,`timestamp`,`id`)
            SELECT COALESCE(`content`,''), COALESCE(`forumname`,''),
                   COALESCE(`timestamp`,0), `id`
            FROM `searchposthistory`
        """)
        db.execSQL("DROP TABLE `searchposthistory`")
        db.execSQL("ALTER TABLE `searchposthistory_new` RENAME TO `searchposthistory`")
        db.execSQL("""
            DELETE FROM `searchposthistory` WHERE `id` NOT IN (
                SELECT MAX(`id`) FROM `searchposthistory` GROUP BY `content`
            )
        """)
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_searchposthistory_content` ON `searchposthistory` (`content`)")
    }

    private fun rebuildTopForum(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `topforum_new` (
                `forumid` TEXT NOT NULL,
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
            )
        """)
        db.execSQL("""
            INSERT INTO `topforum_new` (`forumid`,`id`)
            SELECT COALESCE(`forumid`,''), `id`
            FROM `topforum`
        """)
        db.execSQL("DROP TABLE `topforum`")
        db.execSQL("ALTER TABLE `topforum_new` RENAME TO `topforum`")
        db.execSQL("""
            DELETE FROM `topforum` WHERE `id` NOT IN (
                SELECT MAX(`id`) FROM `topforum` GROUP BY `forumid`
            )
        """)
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_topforum_forumid` ON `topforum` (`forumid`)")
    }
}
