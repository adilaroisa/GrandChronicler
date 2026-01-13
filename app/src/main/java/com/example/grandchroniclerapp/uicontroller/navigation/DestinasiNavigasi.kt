package com.example.grandchroniclerapp.uicontroller.navigation

import com.example.grandchroniclerapp.R

interface DestinasiNavigasi {
    val route: String
    val titleRes: Int
}

object DestinasiLogin : DestinasiNavigasi {
    override val route = "login"
    override val titleRes = R.string.login_title
}

object DestinasiRegister : DestinasiNavigasi {
    override val route = "register"
    override val titleRes = R.string.register_title
}

object DestinasiHome : DestinasiNavigasi {
    override val route = "home"
    override val titleRes = R.string.home_title // Atau R.string.app_name
}

object DestinasiSearch : DestinasiNavigasi {
    override val route = "search"
    override val titleRes = R.string.search_title
}

object DestinasiUpload : DestinasiNavigasi {
    override val route = "upload"
    override val titleRes = R.string.upload_title
}

object DestinasiProfile : DestinasiNavigasi {
    override val route = "profile"
    override val titleRes = R.string.profile_title
}

object DestinasiDetail : DestinasiNavigasi {
    override val route = "detail_article"
    override val titleRes = R.string.detail_title
    const val articleIdArg = "articleId"
    val routeWithArg = "$route/{$articleIdArg}"
}

object DestinasiEditArticle : DestinasiNavigasi {
    override val route = "edit_article"
    override val titleRes = R.string.edit_article // Ini jadi benar (Int ketemu Int)

    val articleId = "articleId"
    val routeWithArgs = "$route/{$articleId}"
}

object DestinasiAbout : DestinasiNavigasi {
    override val route = "about_screen"
    override val titleRes = R.string.about_title
}