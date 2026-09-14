package dev.zad1ag.athenaeum

import android.app.Application
import java.io.File

class TarotApp : Application() {
    companion object {
        lateinit var core: TarotCore
            private set
        lateinit var overrides: UserOverrides
    }

    override fun onCreate() {
        super.onCreate()
        val dbFile = File(filesDir, "tarot.db").absolutePath
        core = TarotCore(this).apply {
            init(dbFile)
        }
        overrides = UserOverrides(this)
    }
}
