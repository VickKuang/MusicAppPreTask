package cn.kwq.pretask.logic.dto

import cn.kwq.pretask.logic.db.entity.SongEntity

data class PlayingSongMsgDTO(
    var song:SongEntity?,
    var isPlaying:Boolean,
    var isAlive:Boolean,
    var songSize:Int?,
    var songCurrent:Int?
)
