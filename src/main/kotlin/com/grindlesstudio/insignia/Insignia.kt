package com.grindlesstudio.insignia

import net.neoforged.fml.common.Mod

@Mod(Insignia.MOD_ID)
class Insignia {
    companion object {
        const val MOD_ID = "insignia"
    }

    init {
        PermissionManager.load()
    }
}