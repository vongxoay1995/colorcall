package com.colorcall.callerscreen.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class DatabaseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository
    val contactDao: ContactDao = AppDatabase.getDatabase(application).contactDao()
    val backgroundDao: BackgroundDao = AppDatabase.getDatabase(application).backgroundDao()
    private val allBackgrounds: LiveData<List<Background>>
    init {
        repository = AppRepository(backgroundDao, contactDao)
        allBackgrounds = backgroundDao.getAllBackgrounds()
    }

    fun getAllBackgrounds(): LiveData<List<Background>> {
        return allBackgrounds
    }

    fun insertBackground(background: Background) {
        viewModelScope.launch {
            backgroundDao.insertBackground(background)
        }
    }

    fun deleteBackground(background: Background) = viewModelScope.launch {
        repository.deleteBackground(background)
    }

    fun deleteContact(contact: Contact) = viewModelScope.launch {
        repository.deleteContact(contact)
    }

    fun getContactsByContactId(contactId: String): LiveData<List<Contact>> {
        val contacts = MutableLiveData<List<Contact>>()
        viewModelScope.launch {
            contacts.value = repository.getContactsByContactId(contactId)
        }
        return contacts
    }
    fun getContactsByBackgroundPath(backgroundPath: String): LiveData<List<Contact>> {
        return contactDao.getContactsByBackgroundPath(backgroundPath)
    }
    fun getContactById(contactId: String): Contact? {
        return runBlocking {
            withContext(Dispatchers.IO) {
                contactDao.getContactById(contactId)
            }
        }
    }
    fun updateContact(contact: Contact) {
        viewModelScope.launch {
            contactDao.update(contact)
        }
    }
    fun insertContact(contact: Contact) {
        viewModelScope.launch {
            contactDao.insert(contact)
        }
    }
}