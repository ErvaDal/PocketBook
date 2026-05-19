-- 1. UUID Eklentisini Aktif Et (UUID'leri otomatik oluşturmak için)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. Özel Veri Tipleri (ENUM)
CREATE TYPE account_status_enum AS ENUM ('NORMAL', 'BANNED', 'DELETED');
CREATE TYPE story_status_enum AS ENUM ('DRAFT', 'PUBLISHED', 'COMPLETED', 'DELETED');
create type user_role_enum as enum ('USER', 'ADMIN');

-- 3. CORE TABLOLAR
CREATE TABLE "Users" (
    "UserID" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "Username" VARCHAR(255) NOT NULL,
    "Email" VARCHAR(255) UNIQUE NOT NULL,
    "PasswordHash" VARCHAR(255) NOT NULL,
    "BirthDate" DATE,
    "Status" account_status_enum DEFAULT 'NORMAL',
    "UserRole" user_role_enum DEFAULT 'USER',
    "CreatedAt" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "Stories" (
    "StoryID" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "AuthorID" UUID NOT NULL REFERENCES "Users"("UserID") ON DELETE CASCADE,
    "Title" VARCHAR(255) NOT NULL,
    "Summary" VARCHAR(2000),
    "CoverImageURL" VARCHAR(500),
    "Status" story_status_enum DEFAULT 'DRAFT',
    "AgeLimit" INT DEFAULT 0,
    "CreatedAt" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "Chapters" (
    "ChapterID" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "StoryID" UUID NOT NULL REFERENCES "Stories"("StoryID") ON DELETE CASCADE,
    "ChapterNumber" INT NOT NULL,
    "ModifiedDate" DATE,
    "Content" TEXT NOT NULL, -- Uygulama tarafında 50.000 karakter limiti konacak
    "StarCount" INT DEFAULT 0,
    "CommentCount" INT DEFAULT 0,
    "PublishedAt" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. KÜTÜPHANE MODÜLÜ
CREATE TABLE "UserLibraries" (
    "LibraryID" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "UserID" UUID NOT NULL REFERENCES "Users"("UserID") ON DELETE CASCADE,
    "LibraryName" VARCHAR(255) NOT NULL,
    "IsPublic" BOOLEAN DEFAULT TRUE,
    "CreatedAt" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "LibraryItems" (
    "LibraryID" UUID REFERENCES "UserLibraries"("LibraryID") ON DELETE CASCADE,
    "StoryID" UUID REFERENCES "Stories"("StoryID") ON DELETE CASCADE,
    "AddedAt" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("LibraryID", "StoryID")
);

-- 5. ETKİLEŞİM VE YORUM MODÜLÜ
CREATE TABLE "ChapterComments" (
    "CommentID" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "ChapterID" UUID NOT NULL REFERENCES "Chapters"("ChapterID") ON DELETE CASCADE,
    "UserID" UUID NOT NULL REFERENCES "Users"("UserID") ON DELETE CASCADE,
    "Content" VARCHAR(1000) NOT NULL,
    "ParentCommentID" UUID REFERENCES "ChapterComments"("CommentID") ON DELETE CASCADE,
    "StarCount" INT DEFAULT 0,
    "CreatedAt" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "ChapterStars" (
    "ChapterID" UUID REFERENCES "Chapters"("ChapterID") ON DELETE CASCADE,
    "UserID" UUID REFERENCES "Users"("UserID") ON DELETE CASCADE,
    PRIMARY KEY ("ChapterID", "UserID")
);

CREATE TABLE "CommentStars" (
    "CommentID" UUID REFERENCES "ChapterComments"("CommentID") ON DELETE CASCADE,
    "UserID" UUID REFERENCES "Users"("UserID") ON DELETE CASCADE,
    PRIMARY KEY ("CommentID", "UserID")
);

-- 6. ANALİTİK MODÜLÜ
CREATE TABLE "ReadLogs" (
    "LogID" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "UserID" UUID NOT NULL REFERENCES "Users"("UserID") ON DELETE CASCADE,
    "ChapterID" UUID NOT NULL REFERENCES "Chapters"("ChapterID") ON DELETE CASCADE,
    "ReadDurationSeconds" INT NOT NULL,
    "CreatedAt" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. SOSYAL AKIŞ (TIMELINE & PROFILE BOARD)
CREATE TABLE "TimelinePosts" (
    "PostID" VARCHAR(50) PRIMARY KEY CHECK ("PostID" ~ '^T[a-zA-Z0-9\-]+$'),
    "AuthorID" UUID NOT NULL REFERENCES "Users"("UserID") ON DELETE CASCADE,
    "Content" VARCHAR(1000) NOT NULL,
    "ImageURLs" JSONB,
    "ParentPostID" VARCHAR(50) REFERENCES "TimelinePosts"("PostID") ON DELETE CASCADE,
    "StarCount" INT DEFAULT 0,
    "CreatedAt" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "ProfileBoardPosts" (
    "PostID" VARCHAR(50) PRIMARY KEY CHECK ("PostID" ~ '^PB[a-zA-Z0-9\-]+$'),
    "ProfileOwnerID" UUID NOT NULL REFERENCES "Users"("UserID") ON DELETE CASCADE,
    "AuthorID" UUID NOT NULL REFERENCES "Users"("UserID") ON DELETE CASCADE,
    "Content" VARCHAR(1000) NOT NULL,
    "ImageURLs" JSONB,
    "ParentPostID" VARCHAR(50) REFERENCES "ProfileBoardPosts"("PostID") ON DELETE CASCADE,
    "StarCount" INT DEFAULT 0,
    "CreatedAt" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "Stars" (
    "PostID" VARCHAR(50) NOT NULL CHECK ("PostID" ~ '^(T|PB)[a-zA-Z0-9\-]+$'),
    "UserID" UUID NOT NULL REFERENCES "Users"("UserID") ON DELETE CASCADE,
    PRIMARY KEY ("PostID", "UserID")
    -- PostID Polymorphic olduğu için kesin Foreign Key kısıtlaması (REFERENCES) eklenmedi.
);

-- 8. MESAJLAŞMA (DM) MODÜLÜ
CREATE TABLE "Conversations" (
    "ConversationID" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "User1ID" UUID NOT NULL REFERENCES "Users"("UserID") ON DELETE CASCADE,
    "User2ID" UUID NOT NULL REFERENCES "Users"("UserID") ON DELETE CASCADE,
    "LastMessageAt" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "CreatedAt" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_order CHECK ("User1ID" < "User2ID"),
    UNIQUE ("User1ID", "User2ID")
);

CREATE TABLE "DirectMessages" (
    "MessageID" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "ConversationID" UUID NOT NULL REFERENCES "Conversations"("ConversationID") ON DELETE CASCADE,
    "SenderID" UUID NOT NULL REFERENCES "Users"("UserID") ON DELETE CASCADE,
    "Content" VARCHAR(1000) NOT NULL,
    "IsRead" BOOLEAN DEFAULT FALSE,
    "CreatedAt" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- 9. CHAPTER (BÖLÜM) YORUM SAYISI TETİKLEYİCİSİ
CREATE OR REPLACE FUNCTION update_chapter_comment_count()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        UPDATE "Chapters"
        SET "CommentCount" = "CommentCount" + 1
        WHERE "ChapterID" = NEW."ChapterID";
        RETURN NEW;
    ELSIF (TG_OP = 'DELETE') THEN
        UPDATE "Chapters"
        SET "CommentCount" = "CommentCount" - 1
        WHERE "ChapterID" = OLD."ChapterID";
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_chapter_comment_count
AFTER INSERT OR DELETE ON "ChapterComments"
FOR EACH ROW EXECUTE FUNCTION update_chapter_comment_count();


-- 10. CHAPTER (BÖLÜM) BEĞENİ SAYISI TETİKLEYİCİSİ
CREATE OR REPLACE FUNCTION update_chapter_star_count()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        UPDATE "Chapters"
        SET "StarCount" = "StarCount" + 1
        WHERE "ChapterID" = NEW."ChapterID";
        RETURN NEW;
    ELSIF (TG_OP = 'DELETE') THEN
        UPDATE "Chapters"
        SET "StarCount" = "StarCount" - 1
        WHERE "ChapterID" = OLD."ChapterID";
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_chapter_star_count
AFTER INSERT OR DELETE ON "ChapterStars"
FOR EACH ROW EXECUTE FUNCTION update_chapter_star_count();


-- 11. YORUM BEĞENİ SAYISI TETİKLEYİCİSİ
CREATE OR REPLACE FUNCTION update_comment_star_count()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        UPDATE "ChapterComments"
        SET "StarCount" = "StarCount" + 1
        WHERE "CommentID" = NEW."CommentID";
        RETURN NEW;
    ELSIF (TG_OP = 'DELETE') THEN
        UPDATE "ChapterComments"
        SET "StarCount" = "StarCount" - 1
        WHERE "CommentID" = OLD."CommentID";
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_comment_star_count
AFTER INSERT OR DELETE ON "CommentStars"
FOR EACH ROW EXECUTE FUNCTION update_comment_star_count();


-- 12. TIMELINE VE PROFILE BOARD (SOSYAL AKIŞ) BEĞENİ SAYISI TETİKLEYİCİSİ
-- Not: Burada PostID'nin prefixine (T veya PB) göre hangi tablonun güncelleneceği seçilir.
CREATE OR REPLACE FUNCTION update_post_star_count()
RETURNS TRIGGER AS $$
DECLARE
    target_id VARCHAR;
BEGIN
    IF (TG_OP = 'INSERT') THEN
        target_id := NEW."PostID";
        IF target_id LIKE 'T%' THEN
            UPDATE "TimelinePosts" SET "StarCount" = "StarCount" + 1 WHERE "PostID" = target_id;
        ELSIF target_id LIKE 'PB%' THEN
            UPDATE "ProfileBoardPosts" SET "StarCount" = "StarCount" + 1 WHERE "PostID" = target_id;
        END IF;
        RETURN NEW;
    ELSIF (TG_OP = 'DELETE') THEN
        target_id := OLD."PostID";
        IF target_id LIKE 'T%' THEN
            UPDATE "TimelinePosts" SET "StarCount" = "StarCount" - 1 WHERE "PostID" = target_id;
        ELSIF target_id LIKE 'PB%' THEN
            UPDATE "ProfileBoardPosts" SET "StarCount" = "StarCount" - 1 WHERE "PostID" = target_id;
        END IF;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_post_star_count
AFTER INSERT OR DELETE ON "Stars"
FOR EACH ROW EXECUTE FUNCTION update_post_star_count();


-- 13. CONVERSATIONS LAST MESSAGE AT (SON MESAJ ZAMANI) TETİKLEYİCİSİ
CREATE OR REPLACE FUNCTION update_last_message_at()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE "Conversations"
    SET "LastMessageAt" = NEW."CreatedAt"
    WHERE "ConversationID" = NEW."ConversationID";
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_last_message_at
AFTER INSERT ON "DirectMessages"
FOR EACH ROW EXECUTE FUNCTION update_last_message_at();