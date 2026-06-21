package ke.co.smartroundclinic.patient.koin

import ke.co.smartroundclinic.patient.core.notification.setupNotificationListener
import ke.co.smartroundclinic.patient.core.presence.setupPresenceService
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

fun initKoin(
    appDeclaration: KoinAppDeclaration = {},
    vararg extraModules: Module = emptyArray(),
): KoinApplication = startKoin {
    appDeclaration()
    modules(coreModule, repositoryModule, useCaseModule, *extraModules)
}.also {
    setupNotificationListener()
    setupPresenceService()
}
