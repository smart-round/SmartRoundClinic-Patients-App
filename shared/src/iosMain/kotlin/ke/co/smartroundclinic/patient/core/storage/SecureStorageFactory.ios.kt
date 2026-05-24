package ke.co.smartroundclinic.patient.core.storage

import com.liftric.kvault.KVault

actual fun createKVault(): KVault = KVault(serviceName = "ke.co.smartroundclinic.patient", accessGroup = null)
