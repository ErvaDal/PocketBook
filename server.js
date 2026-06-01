require('dotenv').config();

const express = require('express');
const cors = require('cors');
const pool = require('./config/db');

const app = express();

// Middleware Tanımlamaları
app.use(cors());
app.use(express.json());

// --- ROTA (ROUTE) TANIMLAMALARI ---
const authRoutes = require('./routes/auth');
const storyRoutes = require('./routes/stories'); 
const userRoutes = require('./routes/users');     

app.use('/auth', authRoutes);
app.use('/api/stories', storyRoutes); 
app.use('/api/users', userRoutes);     


// --- SUNUCU TEST ROTASI ---
app.get('/', (req, res) => {
    res.send('Papirus Backend çalışıyor 🚀');
});


// --- SUNUCUYU BAŞLATMA ---
const PORT = process.env.PORT || 8080; 
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});