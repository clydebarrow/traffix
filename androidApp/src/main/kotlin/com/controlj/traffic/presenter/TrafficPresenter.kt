/*
 * Copyright (c) 2021.  Control-J Pty Ltd
 * All rights reserved
 */

package com.controlj.traffic.presenter

import com.controlj.rx.observeOnMainBy
import com.controlj.settings.AviationUnitSettings
import com.controlj.traffic.TrafficSource
import com.controlj.traffic.TrafficTarget
import com.controlj.view.ListPresenter

class TrafficPresenter : ListPresenter<TrafficPresenter.Target>() {

    data class Target(
        val address: TrafficTarget.EmitterAddress,
        val callsign: String,
        val distance: Double,
        val altitude: Double,
        val category: TrafficTarget.EmitterCategory

    ) : Comparable<Target> {
        constructor(target: TrafficTarget) : this(
            target.address,
            target.callSign,
            target.distance,
            target.altitude,
            target.emitterCategory
        )

        override fun compareTo(other: Target): Int {
            return when {
                distance < other.distance -> -1
                distance > other.distance -> 1
                else -> 0
            }
        }
    }

    interface TrafficView {
        fun setCallsign(value: String)
        fun setDistance(value: String)
        fun setAltitude(value: String)
        fun setType(value: String)

        fun set(item: Target) {
            setCallsign(item.callsign)
            setType(item.category.name)
            AviationUnitSettings.apply {
                distanceUnitSetting.value.let {
                    setDistance(it.toString(item.distance) + it.unitName)
                }
                altitudeUnitSetting.value.let {
                    setAltitude(it.toString(item.altitude) + it.unitName)
                }
            }
        }

        fun setHeader() {
            setCallsign("Callsign")
            setDistance("Distance")
            setAltitude("Altitude")
            setType("Type")
        }
    }

    override val keySelector: (Target) -> String = { it.address.toString() }

    init {
        // Using Observable.defer for compatibility with ListPresenter base class
        addSection(
            "Traffic",
            io.reactivex.rxjava3.core.Observable.defer {
                io.reactivex.rxjava3.core.Observable.fromIterable(
                    (TrafficSource.allTargets.map { Target(it) })
                        .sortedByDescending { it.distance }
                )
            }
        )
    }

    override fun onStart() {
        super.onStart()
        // Keep RxJava for TrafficSource.observer since it's from external library
        disposables.add(
            TrafficSource.observer.observeOnMainBy { list ->
                refresh()
            }
        )
    }
}
