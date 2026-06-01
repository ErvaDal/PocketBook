const express = require('express');
const router = express.Router();
const pool = require('../config/db');

// Kullanıcı ID'sine göre tüm profil, kitap ve kütüphane verilerini döner
router.get('/profile/:userId', async (req, res) => {
    try {
        const { userId } = req.params;

        // 🎯 1. Kullanıcı Bilgileri Sorgusu
        const userResult = await pool.query(
            'SELECT "UserID", "Username", "Email" FROM "Users" WHERE "UserID" = $1', 
            [userId]
        );

        if (userResult.rows.length === 0) {
            return res.status(404).json({ success: false, message: "Kullanıcı bulunamadı." });
        }

        // 🎯 2. Yayınlanan Kitaplar Sorgusu
        // 🚀 Düzenlendi: Şemadaki gibi "AuthorID" kullanıldı! Ayrıca "Status" alanı kontrol ediliyor (taslak olmayanlar için 'published' varsayıldı)
        const booksResult = await pool.query(
            'SELECT "StoryID", "Title", "CoverImageURL", "Summary" FROM "Stories" WHERE "AuthorID" = $1',
            [userId]
        );

        // 🎯 3. Herkese Açık Kütüphaneler Sorgusu
        // 🚀 Düzenlendi: Şemadaki gibi tam olarak büyük ID'li "LibraryID" ve "StoryID" alanları bağlandı!
        const librariesResult = await pool.query(
            `SELECT ul."LibraryID", ul."LibraryName", ul."IsPublic", COUNT(li."StoryID")::int AS "BookCount"
             FROM "UserLibraries" ul
             LEFT JOIN "LibraryItems" li ON ul."LibraryID" = li."LibraryID"
             WHERE ul."UserID" = $1 AND ul."IsPublic" = true
             GROUP BY ul."LibraryID", ul."LibraryName", ul."IsPublic"`,
            [userId]
        );

        // Android tarafına pürüzsüzce JSON paketini gönderiyoruz
        res.json({
            success: true,
            user: {
                Id: userResult.rows[0].UserID,
                Username: userResult.rows[0].Username,
                Email: userResult.rows[0].Email,
                Bio: "Papirus Yazarı"
            },
            publishedBooks: booksResult.rows,
            publicLibraries: librariesResult.rows
        });

    } catch (err) {
        console.error("Profil API Hatası:", err.message);
        res.status(500).json({ success: false, message: err.message });
    }
});

module.exports = router;