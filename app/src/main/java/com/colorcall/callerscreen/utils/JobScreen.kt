package com.colorcall.callerscreen.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow

class JobScreen {
    private var progress = -1
    private val max = 100
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var loopingFlowJob: Job? = null
    private var loopingFlow = flow {
        while (true) {
            emit(Unit)
            delay(80L)
        }
    }

    fun startJob(jobProgress: JobProgress?) {
        stopJob()
        if (progress >= max) {
            jobProgress?.onProgress(progress)
            return
        }
        loopingFlowJob = coroutineScope.launch(Dispatchers.IO) {
            loopingFlow.collect {
                progress++
                if (progress > max) {
                    stopJob()
                }
                withContext(Dispatchers.Main) {
                    jobProgress?.onProgress(progress)
                }
            }
        }
    }

    fun stopJob() {
        loopingFlowJob?.cancel()
    }

    fun isProgressMax(): Boolean {
        return progress >= max
    }

    interface JobProgress {
        fun onProgress(count: Int)
    }
}