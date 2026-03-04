package com.huanchengfly.tieba.post.services

import android.annotation.TargetApi
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.huanchengfly.tieba.post.utils.TiebaUtil

@TargetApi(24)
class OKSignTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.let {
            it.state = Tile.STATE_INACTIVE
            it.updateTile()
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        qsTile?.let {
            it.state = Tile.STATE_INACTIVE
            it.updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        TiebaUtil.startSign(this)
    }
}
