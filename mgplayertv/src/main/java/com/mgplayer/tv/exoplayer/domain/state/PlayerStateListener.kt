package com.ayflix.exoplayer.domain.state

interface PlayerStateListener {
    fun on(state: PlayerState)
}