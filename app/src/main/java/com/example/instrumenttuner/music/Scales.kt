package com.example.instrumenttuner.music

data class ScaleTone(val label: String, val cents: Float)
data class Scale(val name: String, val region: String, val tones: List<ScaleTone>, val note: String = "")

val SCALES = listOf(
    Scale("Rast","Maqam", listOf(
        ScaleTone("Root (tonic)",0f), ScaleTone("2nd",204f), ScaleTone("3rd ½♭",342f),
        ScaleTone("4th",498f), ScaleTone("5th",702f), ScaleTone("6th",906f), ScaleTone("7th ½♭",1044f)),
        "Just-intonation approximation; regional variants differ."),
    Scale("Nahawand","Maqam", listOf(
        ScaleTone("Root",0f), ScaleTone("2nd",200f), ScaleTone("♭3rd",300f),
        ScaleTone("4th",500f), ScaleTone("5th",700f), ScaleTone("♭6th",800f), ScaleTone("♭7th",1000f)),
        "Minor-family maqam; close to 12-TET."),
    Scale("Hijaz","Maqam", listOf(
        ScaleTone("Root",0f), ScaleTone("2nd ½♭",100f), ScaleTone("3rd",300f),
        ScaleTone("4th",500f), ScaleTone("5th",700f), ScaleTone("♭6th",800f), ScaleTone("♭7th",1000f)),
        "Half-flat 2nd; exact intonation varies by school."),
    Scale("Bhairav","Raga", listOf(
        ScaleTone("Sa",0f), ScaleTone("komal Re",100f), ScaleTone("Ga",400f),
        ScaleTone("Ma",500f), ScaleTone("Pa",700f), ScaleTone("komal Dha",800f), ScaleTone("Ni",1100f)),
        "Morning raga; komal Re and Dha."),
    Scale("Yaman","Raga", listOf(
        ScaleTone("Sa",0f), ScaleTone("Re",200f), ScaleTone("Ga",400f),
        ScaleTone("tivra Ma",600f), ScaleTone("Pa",700f), ScaleTone("Dha",900f), ScaleTone("Ni",1100f)),
        "Evening raga; tivra (sharp) Ma."),
    Scale("Bhairavi","Raga", listOf(
        ScaleTone("Sa",0f), ScaleTone("komal Re",100f), ScaleTone("komal Ga",300f),
        ScaleTone("Ma",500f), ScaleTone("Pa",700f), ScaleTone("komal Dha",800f), ScaleTone("komal Ni",1000f)),
        "All komal except Sa, Ma, Pa."),
    Scale("Hirajōshi","Japanese", listOf(
        ScaleTone("Root",0f), ScaleTone("2nd",200f), ScaleTone("♭3rd",300f),
        ScaleTone("5th",700f), ScaleTone("♭6th",800f)),
        "Koto / shakuhachi family."),
    Scale("Yō scale","Japanese", listOf(
        ScaleTone("Root",0f), ScaleTone("2nd",200f), ScaleTone("4th",500f),
        ScaleTone("5th",700f), ScaleTone("6th",900f)),
        "Bright pentatonic."),
    Scale("Slendro (approx.)","Gamelan", listOf(
        ScaleTone("1 (barang)",0f), ScaleTone("2",240f), ScaleTone("3",480f),
        ScaleTone("5",720f), ScaleTone("6",960f)),
        "Equal-step approximation — every gamelan's slendro is unique.")
)
