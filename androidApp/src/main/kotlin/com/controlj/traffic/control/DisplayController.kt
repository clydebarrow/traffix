/*
 * Copyright (c) 2022.  Control-J Pty Ltd
 * All rights reserved
 */

package com.controlj.traffic.control

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.controlj.location.LocationProvider
import com.controlj.location.LocationSource
import com.controlj.logging.CJLog.logException
import com.controlj.logging.CJLog.logMsg
import com.controlj.rx.finaliseUI
import com.controlj.rx.observeOnMainBy
import com.controlj.stratux.Stratux
import com.controlj.traffic.view.RouteGroup
import com.controlj.widget.MessageBank
import com.controlj.widget.MessageView
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.*

/**
 * Drives the visible display
 *
 */
object DisplayController {
    val messageBank = MessageBank()
    val messageView = MessageView(messageBank)
    val routeGroup = RouteGroup()

    private val disposables = mutableListOf<Disposable>()
    private val jobs = mutableListOf<Job>()

    /**
     * Invalidate displayed data
     */

    fun onStop() {
        jobs.forEach { it.cancel() }
        jobs.clear()
        disposables.forEach { it.dispose() }
        disposables.clear()
        LocationSource.remove(LocationProvider.deviceProvider)
    }

    fun onStart(owner: LifecycleOwner) {
        jobs.forEach { it.cancel() }
        jobs.clear()
        disposables.forEach { it.dispose() }
        disposables.clear()
        logMsg("onStart")
        
        // Keep RxJava for messageBank.observable since it's from external library
        disposables.add(messageBank.observable.observeOnMainBy {
            messageView.requestRedraw()
        })
        messageView.requestRedraw()
        
        // Replace Observable.interval with coroutine timer
        // Executes immediately on first iteration, then after each 1-second delay
        jobs.add(owner.lifecycleScope.launch(Dispatchers.Main) {
            while (isActive) {
                routeGroup.invalidateUtc()
                delay(1000) // 1 second
            }
        })
        
        LocationSource.add(LocationProvider.deviceProvider)
        
        // Keep RxJava for LocationSource.observer since it's from external library
        disposables.add(LocationSource.observer
            .finaliseUI(routeGroup::invalidateData)
            .observeOnMainBy { data ->
                try {
                    routeGroup.update(data)
                } catch (ex: Exception) {
                    logException(ex)
                }
            }
        )
    }
}
