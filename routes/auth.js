const express = require('express');
const router = express.Router();
const pool = require('../config/db');
const bcrypt = require('bcrypt');

// 1. REGISTER (Kayıt Olma)
router.post('/register', async (req, res) => {
    try {
        const { username, email, password, birthDate } = req.body;

        // E-posta veya kullanıcı adı zaten var mı kontrolü
        const userExists = await pool.query(
            'SELECT * FROM "Users" WHERE "Email" = $1 OR "Username" = $2', 
            [email, username]
        );
        
        if (userExists.rows.length > 0) {
            return res.status(400).json({ 
                success: false, 
                message: "Bu kullanıcı adı veya e-posta zaten kullanımda." 
            });
        }

        // Şifreyi güvenli bir şekilde hash'le
        const hashedPassword = await bcrypt.hash(password, 10);

        // Kullanıcıyı veritabanına ekle
        const result = await pool.query(
            `INSERT INTO "Users"
            ("Username", "Email", "PasswordHash", "BirthDate")
            VALUES ($1, $2, $3, $4)
            RETURNING *`,
            [username, email, hashedPassword, birthDate]
        );

        res.json({
            success: true,
            message: "Kullanıcı başarıyla oluşturuldu.",
            user: result.rows[0]
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({
            success: false,
            message: err.message
        });
    }
});

// 2. LOGIN (Giriş Yapma)
router.post('/login', async (req, res) => {
    try {
        const { email, password } = req.body;
        console.log("---------------- GİRİŞ İSTEĞİ ----------------");
        console.log("Android'den Gelen E-posta:", email);
        console.log("Android'den Gelen Şifre:", password);

        // Kullanıcıyı veritabanında e-posta adresine göre ara (Büyük/küçük harfe duyarsız)
        const result = await pool.query('SELECT * FROM "Users" WHERE "Email" ILIKE $1', [email]);
        
        // Eğer e-posta veritabanında yoksa
        if (result.rows.length === 0) {
            console.log("❌ HATA: Bu e-posta veritabanında hiç yok!");
            return res.status(400).json({ success: false, message: "E-posta veya şifre hatalı." });
        }

        const user = result.rows[0];

        //
        console.log("✅ E-posta veritabanında bulundu!");
        console.log("DB'deki Tam Sütunlar:", user);
        console.log("DB'deki Hash'li Şifre:", user.PasswordHash);

        // Android'den gelen düz şifre ile veritabanındaki Hash'lenmiş şifreyi karşılaştır
        const isPasswordMatch = await bcrypt.compare(password, user.PasswordHash);
        
        console.log("Şifre Eşleşme Sonucu:", isPasswordMatch);

        if (!isPasswordMatch) {
            console.log("❌ HATA: Şifreler uyuşmadı!");
            return res.status(400).json({ success: false, message: "E-posta veya şifre hatalı." });
        }

        // Giriş başarılı ise Android tarafındaki SharedPreferences'ın beklediği verileri dön
        res.json({
            success: true,
            message: "Giriş başarılı!",
            user: {
                id: user.UserID,
                username: user.Username,
                email: user.Email
            }
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ 
            success: false, 
            message: "Sunucu hatası oluştu." 
        });
    }
});

// 3. FORGOT PASSWORD (Şifremi Unuttum)
router.post('/forgot-password', async (req, res) => {
    try {
        const { email } = req.body;

        // E-postanın veritabanında var olup olmadığını kontrol et
        const result = await pool.query('SELECT * FROM "Users" WHERE "Email" = $1', [email]);
        
        if (result.rows.length === 0) {
            return res.status(404).json({ 
                success: false, 
                message: "Bu e-posta adresine kayıtlı bir kullanıcı bulunamadı." 
            });
        }

        // Simüle edilmiş başarılı yanıt
        res.json({
            success: true,
            message: "Şifre sıfırlama bağlantısı e-posta adresinize gönderildi!"
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ 
            success: false, 
            message: "Sunucu hatası oluştu." 
        });
    }
});

module.exports = router;