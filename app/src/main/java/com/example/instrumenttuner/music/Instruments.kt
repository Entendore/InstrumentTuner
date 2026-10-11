package com.example.instrumenttuner.music

data class InstrumentString(val label: String, val note: Note)
data class Instrument(val name: String, val strings: List<InstrumentString>, val region: String = "Western")

val INSTRUMENTS = listOf(
    Instrument("Guitar", listOf(
        InstrumentString("6 · E", Note(40)), InstrumentString("5 · A", Note(45)),
        InstrumentString("4 · D", Note(50)), InstrumentString("3 · G", Note(55)),
        InstrumentString("2 · B", Note(59)), InstrumentString("1 · E", Note(64)))),
    Instrument("Guitar (7-string)", listOf(
        InstrumentString("7 · B", Note(23)), InstrumentString("6 · E", Note(28)),
        InstrumentString("5 · A", Note(33)), InstrumentString("4 · D", Note(38)),
        InstrumentString("3 · G", Note(43)), InstrumentString("2 · B", Note(47)),
        InstrumentString("1 · E", Note(52)))),
    Instrument("Bass", listOf(
        InstrumentString("4 · E", Note(28)), InstrumentString("3 · A", Note(33)),
        InstrumentString("2 · D", Note(38)), InstrumentString("1 · G", Note(43)))),
    Instrument("Bass (5-string)", listOf(
        InstrumentString("5 · B", Note(23)), InstrumentString("4 · E", Note(28)),
        InstrumentString("3 · A", Note(33)), InstrumentString("2 · D", Note(38)),
        InstrumentString("1 · G", Note(43)))),
    Instrument("Ukulele", listOf(
        InstrumentString("4 · G", Note(67)), InstrumentString("3 · C", Note(60)),
        InstrumentString("2 · E", Note(64)), InstrumentString("1 · A", Note(69)))),
    Instrument("Ukulele (baritone)", listOf(
        InstrumentString("4 · D", Note(50)), InstrumentString("3 · G", Note(55)),
        InstrumentString("2 · B", Note(59)), InstrumentString("1 · E", Note(64)))),
    Instrument("Violin", listOf(
        InstrumentString("", Note(55)), InstrumentString("", Note(62)),
        InstrumentString("", Note(69)), InstrumentString("", Note(76)))),
    Instrument("Viola", listOf(
        InstrumentString("", Note(48)), InstrumentString("", Note(55)),
        InstrumentString("", Note(62)), InstrumentString("", Note(69)))),
    Instrument("Cello", listOf(
        InstrumentString("", Note(36)), InstrumentString("", Note(43)),
        InstrumentString("", Note(50)), InstrumentString("", Note(57)))),
    Instrument("Mandolin", listOf(
        InstrumentString("G", Note(55)), InstrumentString("D", Note(62)),
        InstrumentString("A", Note(69)), InstrumentString("E", Note(76)))),
    Instrument("Irish bouzouki", listOf(
        InstrumentString("4 · G", Note(43)), InstrumentString("3 · D", Note(50)),
        InstrumentString("2 · A", Note(57)), InstrumentString("1 · E", Note(64)))),
    Instrument("Mandola", listOf(
        InstrumentString("4 · C", Note(48)), InstrumentString("3 · G", Note(55)),
        InstrumentString("2 · D", Note(62)), InstrumentString("1 · A", Note(69)))),
    Instrument("Banjo (5-string, open G)", listOf(
        InstrumentString("5 · g", Note(67)), InstrumentString("4 · D", Note(50)),
        InstrumentString("3 · G", Note(55)), InstrumentString("2 · B", Note(59)),
        InstrumentString("1 · D", Note(62)))),
    Instrument("Mountain dulcimer (DAd)", listOf(
        InstrumentString("bass", Note(50)), InstrumentString("middle", Note(57)),
        InstrumentString("melody", Note(62)))),
    Instrument("Balalaika (prima)", listOf(
        InstrumentString("3 · E", Note(64)), InstrumentString("2 · E", Note(64)),
        InstrumentString("1 · A", Note(69)))),
    Instrument("Bouzouki (Greek, 4-course)", listOf(
        InstrumentString("4 · C", Note(48)), InstrumentString("3 · F", Note(53)),
        InstrumentString("2 · A", Note(57)), InstrumentString("1 · D", Note(62)))),
    Instrument("Charango (Andean)", listOf(
        InstrumentString("5 · D", Note(62)), InstrumentString("4 · G", Note(67)),
        InstrumentString("3 · C", Note(72)), InstrumentString("2 · E", Note(64)),
        InstrumentString("1 · A", Note(69)))),
    Instrument("Cuatro venezolano", listOf(
        InstrumentString("4 · A", Note(57)), InstrumentString("3 · D", Note(62)),
        InstrumentString("2 · F♯", Note(66)), InstrumentString("1 · B", Note(59)))),
    Instrument("Cavaquinho (Brazil)", listOf(
        InstrumentString("4 · D", Note(62)), InstrumentString("3 · G", Note(67)),
        InstrumentString("2 · B", Note(71)), InstrumentString("1 · D", Note(74)))),
    Instrument("Guitarrón mexicano", listOf(
        InstrumentString("6 · A", Note(33)), InstrumentString("5 · D", Note(38)),
        InstrumentString("4 · G", Note(43)), InstrumentString("3 · C", Note(48)),
        InstrumentString("2 · F", Note(53)), InstrumentString("1 · A", Note(45)))),
    Instrument("Tiple colombiano (nominal)", listOf(
        InstrumentString("4 · G", Note(55)), InstrumentString("3 · C", Note(60)),
        InstrumentString("2 · E", Note(64)), InstrumentString("1 · A", Note(69)))),
    Instrument("Ronroco (nominal)", listOf(
        InstrumentString("4 · G", Note(55)), InstrumentString("3 · C", Note(60)),
        InstrumentString("2 · E", Note(64)), InstrumentString("1 · A", Note(69)))),
    Instrument("Tres cubano (G–C–E)", listOf(
        InstrumentString("3rd course", Note(55)), InstrumentString("2nd course", Note(60)),
        InstrumentString("1st course", Note(64)))),
    Instrument("Oud (Arabic)", listOf(
        InstrumentString("6 · D", Note(38)), InstrumentString("5 · G", Note(43)),
        InstrumentString("4 · A", Note(45)), InstrumentString("3 · D", Note(50)),
        InstrumentString("2 · G", Note(55)), InstrumentString("1 · C", Note(60)))),
    Instrument("Bağlama / saz (D–G–A)", listOf(
        InstrumentString("alt", Note(50)), InstrumentString("orta", Note(55)),
        InstrumentString("üst", Note(57)))),
    Instrument("Qanun (diatonic base)", listOf(
        InstrumentString("C", Note(48)), InstrumentString("D", Note(50)),
        InstrumentString("E", Note(52)), InstrumentString("F", Note(53)),
        InstrumentString("G", Note(55)), InstrumentString("A", Note(57)),
        InstrumentString("B", Note(59)), InstrumentString("C", Note(60)),
        InstrumentString("D", Note(62)), InstrumentString("E", Note(64)),
        InstrumentString("F", Note(65)), InstrumentString("G", Note(67)),
        InstrumentString("A", Note(69)), InstrumentString("B", Note(71)),
        InstrumentString("C", Note(72)))),
    Instrument("Santur (Persian, D)", listOf(
        InstrumentString("D", Note(50)), InstrumentString("E", Note(52)),
        InstrumentString("F♯", Note(54)), InstrumentString("G", Note(55)),
        InstrumentString("A", Note(57)), InstrumentString("B", Note(59)),
        InstrumentString("C♯", Note(61)), InstrumentString("D", Note(62)))),
    Instrument("Buzuq", listOf(
        InstrumentString("Jawab", Note(60)), InstrumentString("Qarar", Note(55)))),
    Instrument("Tar (Persian)", listOf(
        InstrumentString("3rd course", Note(48)), InstrumentString("2nd course", Note(55)),
        InstrumentString("1st course", Note(60)))),
    Instrument("Setar (Persian)", listOf(
        InstrumentString("4 · C", Note(48)), InstrumentString("3 · C", Note(48)),
        InstrumentString("2 · G", Note(55)), InstrumentString("1 · C", Note(60)))),
    Instrument("Guembri / sintir", listOf(
        InstrumentString("low", Note(50)), InstrumentString("middle", Note(57)),
        InstrumentString("high", Note(62)))),
    Instrument("Kalimba (17-key C)", listOf(
        InstrumentString("C", Note(60)), InstrumentString("D", Note(62)),
        InstrumentString("E", Note(64)), InstrumentString("F", Note(65)),
        InstrumentString("G", Note(67)), InstrumentString("A", Note(69)),
        InstrumentString("B", Note(71)), InstrumentString("C", Note(72)),
        InstrumentString("D", Note(74)), InstrumentString("E", Note(76)),
        InstrumentString("F", Note(77)), InstrumentString("G", Note(79)),
        InstrumentString("A", Note(81)), InstrumentString("B", Note(83)),
        InstrumentString("C", Note(84)), InstrumentString("D", Note(86)),
        InstrumentString("E", Note(88)))),
    Instrument("Kora (silaba tuning)", listOf(
        InstrumentString("L1", Note(53)), InstrumentString("L2", Note(57)),
        InstrumentString("L3", Note(60)), InstrumentString("L4", Note(64)),
        InstrumentString("L5", Note(65)), InstrumentString("L6", Note(69)),
        InstrumentString("L7", Note(72)), InstrumentString("L8", Note(76)),
        InstrumentString("L9", Note(77)), InstrumentString("L10", Note(81)),
        InstrumentString("L11", Note(84)), InstrumentString("R1", Note(59)),
        InstrumentString("R2", Note(62)), InstrumentString("R3", Note(67)),
        InstrumentString("R4", Note(71)), InstrumentString("R5", Note(74)),
        InstrumentString("R6", Note(79)), InstrumentString("R7", Note(83)),
        InstrumentString("R8", Note(86)), InstrumentString("R9", Note(90)),
        InstrumentString("R10", Note(93)))),
    Instrument("Tanpura (drone, Sa = C)", listOf(
        InstrumentString("Pa", Note(55)), InstrumentString("Sa · 1", Note(60)),
        InstrumentString("Sa · 2", Note(60)), InstrumentString("Sa · 3", Note(60)))),
    Instrument("Sitar (Sa = C)", listOf(
        InstrumentString("Sa", Note(48)), InstrumentString("Pa", Note(55)),
        InstrumentString("Sa", Note(60)), InstrumentString("Ma", Note(65)),
        InstrumentString("Sa", Note(72)))),
    Instrument("Veena (Saraswati)", listOf(
        InstrumentString("Sa", Note(48)), InstrumentString("Pa", Note(55)),
        InstrumentString("Sa", Note(60)), InstrumentString("Pa", Note(67)))),
    Instrument("Sarangi (Sa–Pa–Sa)", listOf(
        InstrumentString("3rd", Note(48)), InstrumentString("2nd", Note(55)),
        InstrumentString("1st", Note(60)))),
    Instrument("Ektara (drone)", listOf(InstrumentString("drone", Note(52)))),
    Instrument("Dombra (Kazakh)", listOf(
        InstrumentString("bass", Note(50)), InstrumentString("treble", Note(55)))),
    Instrument("Dutar (quint tuning)", listOf(
        InstrumentString("bamm", Note(50)), InstrumentString("zil", Note(57)))),
    Instrument("Pipa (Chinese)", listOf(
        InstrumentString("IV · A", Note(45)), InstrumentString("III · D", Note(50)),
        InstrumentString("II · E", Note(52)), InstrumentString("I · A", Note(57)))),
    Instrument("Guzheng (D pentatonic)", listOf(
        InstrumentString("D", Note(50)), InstrumentString("E", Note(52)),
        InstrumentString("F♯", Note(54)), InstrumentString("A", Note(57)),
        InstrumentString("B", Note(59)), InstrumentString("D", Note(62)),
        InstrumentString("E", Note(64)), InstrumentString("F♯", Note(66)),
        InstrumentString("A", Note(69)), InstrumentString("B", Note(71)))),
    Instrument("Koto (hirajōshi)", listOf(
        InstrumentString("I", Note(50)), InstrumentString("II", Note(55)),
        InstrumentString("III", Note(58)), InstrumentString("IV", Note(60)),
        InstrumentString("V", Note(62)), InstrumentString("VI", Note(63)),
        InstrumentString("VII", Note(65)), InstrumentString("VIII", Note(69)),
        InstrumentString("IX", Note(70)), InstrumentString("X", Note(72)),
        InstrumentString("XI", Note(74)), InstrumentString("XII", Note(75)),
        InstrumentString("XIII", Note(77)))),
    Instrument("Shamisen (honchōshi)", listOf(
        InstrumentString("ichi", Note(50)), InstrumentString("ni", Note(55)),
        InstrumentString("san", Note(62)))),
    Instrument("Gayageum (Korean)", listOf(
        InstrumentString("E♭", Note(51)), InstrumentString("F", Note(53)),
        InstrumentString("A♭", Note(56)), InstrumentString("B♭", Note(58)),
        InstrumentString("C", Note(60)), InstrumentString("E♭", Note(63)),
        InstrumentString("F", Note(65)), InstrumentString("A♭", Note(68)),
        InstrumentString("B♭", Note(70)), InstrumentString("C", Note(72)),
        InstrumentString("E♭", Note(75)), InstrumentString("F", Note(77)))),
    Instrument("Đàn tranh (C pentatonic)", listOf(
        InstrumentString("C", Note(48)), InstrumentString("D", Note(50)),
        InstrumentString("F", Note(53)), InstrumentString("G", Note(55)),
        InstrumentString("A", Note(57)), InstrumentString("C", Note(60)),
        InstrumentString("D", Note(62)), InstrumentString("F", Note(65)),
        InstrumentString("G", Note(67)), InstrumentString("A", Note(69)))),
    Instrument("Lap steel (C6)", listOf(
        InstrumentString("6 · C", Note(48)), InstrumentString("5 · E", Note(52)),
        InstrumentString("4 · G", Note(55)), InstrumentString("3 · A", Note(57)),
        InstrumentString("2 · C", Note(60)), InstrumentString("1 · E", Note(64)))),
    Instrument("Lyre (D major diatonic)", listOf(
        InstrumentString("D", Note(50)), InstrumentString("E", Note(52)),
        InstrumentString("F♯", Note(54)), InstrumentString("G", Note(55)),
        InstrumentString("A", Note(57)), InstrumentString("B", Note(59)),
        InstrumentString("C♯", Note(61)))),
    Instrument("Celtic harp (C major)", listOf(
        InstrumentString("C", Note(48)), InstrumentString("D", Note(50)),
        InstrumentString("E", Note(52)), InstrumentString("F", Note(53)),
        InstrumentString("G", Note(55)), InstrumentString("A", Note(57)),
        InstrumentString("B", Note(59)), InstrumentString("C", Note(60)),
        InstrumentString("D", Note(62)), InstrumentString("E", Note(64)),
        InstrumentString("F", Note(65)), InstrumentString("G", Note(67)),
        InstrumentString("A", Note(69)), InstrumentString("B", Note(71)),
        InstrumentString("C", Note(72)), InstrumentString("D", Note(74)),
        InstrumentString("E", Note(76)), InstrumentString("F", Note(77)),
        InstrumentString("G", Note(79)), InstrumentString("A", Note(81)),
        InstrumentString("B", Note(83)), InstrumentString("C", Note(84))))
)
