const express = require('express');
const router = express.Router();
const pool = require('../config/db');
const crypto = require('crypto');

// Yeni Kitap ve İlk Bölümü Birlikte Kaydetme API'si (Sütun Sayısı Düzeltildi 🚀)
router.post('/create-with-chapter', async (req, res) => {
    const client = await pool.connect();
    try {
        const { authorId, title, summary, coverImageUrl, chapterTitle, chapterContent } = req.body;

        await client.query('BEGIN'); // Transaction Başlat

        const generatedStoryId = crypto.randomUUID();
        const generatedChapterId = crypto.randomUUID();

        // 🚀 KESİN ÇÖZÜM: Sütun isimleri ve $ parametre sayıları harfi harfine eşitlendi!
        const storyQuery = `
            INSERT INTO "Stories" ("StoryID", "AuthorID", "Title", "Summary", "CoverImageURL", "Status", "AgeLimit")
            VALUES ($1, $2, $3, $4, $5, 'PUBLISHED', 0);
        `;
        // $1: generatedStoryId, $2: authorId, $3: title, $4: summary, $5: coverImageUrl
        await client.query(storyQuery, [generatedStoryId, authorId, title, summary, coverImageUrl || '']);

        // 2. Chapters Tablosuna İlk Bölümü Ekleme
        const chapterQuery = `
            INSERT INTO "Chapters" ("ChapterID", "StoryID", "ChapterNumber", "Content", "StarCount", "CommentCount", "PublishedAt", "ModifiedDate")
            VALUES ($1, $2, 1, $3, 0, 0, NOW(), NOW());
        `;
        
        const fullContent = `<h3>${chapterTitle}</h3>\n\n${chapterContent}`;
        await client.query(chapterQuery, [generatedChapterId, generatedStoryId, fullContent]);

        await client.query('COMMIT'); // Tüm işlemler başarılıysa kaydet

        res.status(201).json({
            success: true,
            message: "Kitap ve ilk bölüm başarıyla yayınlandı!",
            storyId: generatedStoryId
        });

    } catch (err) {
        await client.query('ROLLBACK'); // Hata anında her şeyi geri al
        console.error("❌ VERİTABANI YAZMA HATASI:", err.message);
        res.status(500).json({ success: false, message: err.message });
    } finally {
        client.release();
    }
});

// 🎯 ROTA 1: Ana Sayfa İçin Tüm Kitapları Listeleme
router.get('/all', async (req, res) => {
    try {
        const result = await pool.query(
            `SELECT s.*, u."Username" AS "AuthorName" 
             FROM "Stories" s
             JOIN "Users" u ON s."AuthorID" = u."UserID"
             WHERE s."Status" = 'PUBLISHED'
             ORDER BY s."CreatedAt" DESC`
        );
        res.json({ success: true, books: result.rows });
    } catch (err) {
        console.error(err.message);
        res.status(500).json({ success: false, message: err.message });
    }
});

// 🎯 ROTA 2: Kitap Detayları ve Bölüm Listesini Getirme
router.get('/details/:storyId', async (req, res) => {
    try {
        
        console.log("==========================================");
        console.log("📥 KITAP DETAY ISTEGI GELDI!");
        console.log("🆔 Gelen StoryID Parametresi:", req.params.storyId);
        console.log("==========================================");
        const { storyId } = req.params;

        const storyResult = await pool.query(
            `SELECT s.*, u."Username" AS "AuthorName" 
             FROM "Stories" s
             JOIN "Users" u ON s."AuthorID" = u."UserID"
             WHERE s."StoryID" = $1`, [storyId]
        );

        if (storyResult.rows.length === 0) {
            return res.status(404).json({ success: false, message: "Kitap bulunamadı." });
        }

        const chaptersResult = await pool.query(
            `SELECT "ChapterID", "StoryID", "ChapterNumber", "StarCount", "CommentCount" 
             FROM "Chapters" 
             WHERE "StoryID" = $1 
             ORDER BY "ChapterNumber" ASC`, [storyId]
        );

        res.json({
            success: true,
            story: storyResult.rows[0],
            chapters: chaptersResult.rows
        });
    } catch (err) {
        console.error(err.message);
        res.status(500).json({ success: false, message: err.message });
    }
});

// 🎯 ROTA 3: Belirli Bir Bölümün İçeriğini Okuma (Hatalı Rota Kopya İçeriği Düzeltildi 🚀)
router.get('/chapter/:storyId/:chapterNumber', async (req, res) => {
    try {
        const { storyId, chapterNumber } = req.params;

        // 🚀 KESİN ÇÖZÜM: Buraya yanlışlıkla ana sayfa sorgusu kopyalanmıştı, Chapters tablosuna yönlendirildi!
        const result = await pool.query(
            `SELECT * FROM "Chapters" 
             WHERE "StoryID" = $1 AND "ChapterNumber" = $2`, 
            [storyId, chapterNumber]
        );

        if (result.rows.length === 0) {
            return res.status(404).json({ success: false, message: "Bölüm bulunamadı." });
        }

        res.json({ success: true, chapter: result.rows[0] });
    } catch (err) {
        console.error(err.message);
        res.status(500).json({ success: false, message: err.message });
    }
});

const nodemailer = require('nodemailer'); // Dosyanın en üstüne ekle

// E-Posta Gönderim Köprüsü (Garantili SMTP Ayarı)
const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: 'papirus811@gmail.com', 
        pass: 'nbjxunyvvfsbjusv' 
    },
    
    tls: {
        // Güvenlik duvarının veya yerel ağın sertifika engeline takılmayı önler:
        rejectUnauthorized: false 
    }
});

// ❤️ BEĞENİ VE E-POSTA BİLDİRİM API'Sİ
router.post('/like-chapter', async (req, res) => {
    try {
        const { storyId, chapterNumber, likerUsername } = req.body;

        // 1. Veritabanında o kitabın yazarının e-postasını ve kitap adını buluyoruz
        const authorQuery = `
            SELECT u."Email", s."Title" 
            FROM "Stories" s
            JOIN "Users" u ON s."AuthorID" = u."UserID"
            WHERE s."StoryID" = $1
        `;
        const authorResult = await pool.query(authorQuery, [storyId]);

        if (authorResult.rows.length === 0) {
            return res.status(404).json({ success: false, message: "Yazar bilgisi bulunamadı." });
        }

        const authorEmail = authorResult.rows[0].Email;
        const bookTitle = authorResult.rows[0].Title;

        // 2. Chapters tablosundaki StarCount değerini 1 arttırıyoruz
        await pool.query(
            `UPDATE "Chapters" 
             SET "StarCount" = "StarCount" + 1 
             WHERE "StoryID" = $1 AND "ChapterNumber" = $2`,
            [storyId, chapterNumber]
        );

        // 3. Yazara gidecek e-posta şablonunu hazırlıyoruz
        const mailOptions = {
            from: '"Papirus Bildirim" <papirusapp2026@gmail.com>',
            to: authorEmail,
            subject: `🎉 Harika Haber! "${bookTitle}" Kitabın Beğenildi!`,
            html: `
                <div style="font-family: sans-serif; padding: 20px; border: 1px solid #eee; border-radius: 8px;">
                    <h2 style="color: #FF4A6B;">Papirus'ta Yeni Beğeni! ❤️</h2>
                    <p>Merhaba,</p>
                    <p>Yazdığın <b>"${bookTitle}"</b> isimli kitabının <b>${chapterNumber}. Bölümü</b>, 
                    <span style="color: #4A90E2; font-weight: bold;">@${likerUsername}</span> isimli okurumuz tarafından beğenildi!</p>
                    <p>Kalemine sağlık, okurların senin yeni bölümlerini heyecanla bekliyor!</p>
                    <br>
                    <small style="color: #aaa;">Papirus Geliştirici Ekibi 2026</small>
                </div>
            `
        };

        // 4. E-postayı arka planda fırlatıyoruz (Kullanıcıyı bekletmemek için asenkron)
        transporter.sendMail(mailOptions, (error, info) => {
            if (error) console.error("❌ E-Posta gönderilemedi:", error);
            else console.log("📧 E-Posta başarıyla yazara uçuruldu: " + info.response);
        });

        res.json({ success: true, message: "Beğeni başarıyla işlendi ve e-posta tetiklendi!" });

    } catch (err) {
        console.error(err.message);
        res.status(500).json({ success: false, message: err.message });
    }
});

// 🔍 CANLI ARAMA API'Sİ (Kitap Adı, Yazar Adı veya Kategoriye Göre Arama)
router.get('/search/:query', async (req, res) => {
    try {
        const { query } = req.params;
        
        // ILIKE kullanarak büyük-küçük harf ve Türkçe karakter esnekliği sağlıyoruz
        const searchQuery = `
            SELECT s.*, u."Username" AS "AuthorName" 
            FROM "Stories" s
            JOIN "Users" u ON s."AuthorID" = u."UserID"
            WHERE (s."Title" ILIKE $1 OR u."Username" ILIKE $1) 
              AND s."Status" = 'PUBLISHED'
            ORDER BY s."CreatedAt" DESC
        `;
        
        const result = await pool.query(searchQuery, [`%${query}%`]);
        res.json({ success: true, books: result.rows });
    } catch (err) {
        console.error("❌ ARAMA HATASI:", err.message);
        res.status(500).json({ success: false, message: err.message });
    }
});

// 🔄 1. Kitap Durumunu Güncelleme (Yayınla / Yayından Kaldır)
router.put('/update-status/:storyId', async (req, res) => {
    try {
        const { storyId } = req.params;
        const { status } = req.body; // 'PUBLISHED' veya 'DRAFT' gelecek

        await pool.query(
            `UPDATE "Stories" SET "Status" = $1 WHERE "StoryID" = $2`,
            [status, storyId]
        );
        res.json({ success: true, message: `Kitap durumu ${status} olarak güncellendi.` });
    } catch (err) {
        res.status(500).json({ success: false, message: err.message });
    }
});

// ✍️ 2. Kitap Detaylarını Güncelleme (Açıklama/Özet ve Kapak Görseli)
router.put('/update-details/:storyId', async (req, res) => {
    try {
        const { storyId } = req.params;
        const { summary, coverImageUrl } = req.body;

        await pool.query(
            `UPDATE "Stories" SET "Summary" = $1, "CoverImageURL" = $2 WHERE "StoryID" = $3`,
            [summary, coverImageUrl || '', storyId]
        );
        res.json({ success: true, message: "Kitap bilgileri başarıyla güncellendi." });
    } catch (err) {
        res.status(500).json({ success: false, message: err.message });
    }
});

// 🗑️ 3. Kitabı ve Ona Ait Tüm Bölümleri Veritabanından Tamamen Silme
router.put('/delete-story/:storyId', async (req, res) => {
    const client = await pool.connect();
    try {
        const { storyId } = req.params;
        await client.query('BEGIN');

        // Önce kısıtlamalara takılmamak için kitaba bağlı bölümleri uçuruyoruz
        await client.query(`DELETE FROM "Chapters" WHERE "StoryID" = $1`, [storyId]);
        // Sonra kitabın kendisini siliyoruz
        await client.query(`DELETE FROM "Stories" WHERE "StoryID" = $1`, [storyId]);

        await client.query('COMMIT');
        res.json({ success: true, message: "Kitap ve bağlı tüm bölümler tamamen silindi." });
    } catch (err) {
        await client.query('ROLLBACK');
        res.status(500).json({ success: false, message: err.message });
    } finally {
        client.release();
    }
});

module.exports = router;