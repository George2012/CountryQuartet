package com.countryquartet.game.ui.components

import com.countryquartet.game.R

/**
 * The bundled flag image for a country id, or null when none is bundled.
 *
 * Generated from the files in `res/drawable-nodpi`. The map is explicit rather
 * than looked up by name at runtime so a missing flag is a compile error, not
 * a blank space discovered by a player.
 */
fun flagDrawable(countryId: String): Int? = FLAG_DRAWABLES[countryId.lowercase()]

private val FLAG_DRAWABLES: Map<String, Int> = mapOf(
    "ar" to R.drawable.flag_ar,
    "at" to R.drawable.flag_at,
    "au" to R.drawable.flag_au,
    "bd" to R.drawable.flag_bd,
    "br" to R.drawable.flag_br,
    "ca" to R.drawable.flag_ca,
    "ch" to R.drawable.flag_ch,
    "cl" to R.drawable.flag_cl,
    "cn" to R.drawable.flag_cn,
    "cu" to R.drawable.flag_cu,
    "cz" to R.drawable.flag_cz,
    "de" to R.drawable.flag_de,
    "dk" to R.drawable.flag_dk,
    "dz" to R.drawable.flag_dz,
    "eg" to R.drawable.flag_eg,
    "es" to R.drawable.flag_es,
    "et" to R.drawable.flag_et,
    "fi" to R.drawable.flag_fi,
    "fj" to R.drawable.flag_fj,
    "gr" to R.drawable.flag_gr,
    "hu" to R.drawable.flag_hu,
    "id" to R.drawable.flag_id,
    "il" to R.drawable.flag_il,
    "in" to R.drawable.flag_in,
    "it" to R.drawable.flag_it,
    "jo" to R.drawable.flag_jo,
    "jp" to R.drawable.flag_jp,
    "ke" to R.drawable.flag_ke,
    "kr" to R.drawable.flag_kr,
    "lb" to R.drawable.flag_lb,
    "lk" to R.drawable.flag_lk,
    "ma" to R.drawable.flag_ma,
    "mn" to R.drawable.flag_mn,
    "mx" to R.drawable.flag_mx,
    "my" to R.drawable.flag_my,
    "no" to R.drawable.flag_no,
    "nz" to R.drawable.flag_nz,
    "pe" to R.drawable.flag_pe,
    "pg" to R.drawable.flag_pg,
    "pk" to R.drawable.flag_pk,
    "pl" to R.drawable.flag_pl,
    "pt" to R.drawable.flag_pt,
    "ro" to R.drawable.flag_ro,
    "sa" to R.drawable.flag_sa,
    "se" to R.drawable.flag_se,
    "th" to R.drawable.flag_th,
    "tn" to R.drawable.flag_tn,
    "tz" to R.drawable.flag_tz,
    "ua" to R.drawable.flag_ua,
    "ug" to R.drawable.flag_ug,
    "us" to R.drawable.flag_us,
    "vn" to R.drawable.flag_vn,
)
