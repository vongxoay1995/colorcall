package com.colorcall.callerscreen.database

class AppRepository(
    private val backgroundDao: BackgroundDao,
    private val contactDao: ContactDao
) {

    suspend fun deleteBackground(background: Background) {
        backgroundDao.delete(background)
    }

    suspend fun getContactsByContactId(contactId: String): List<Contact> {
        return contactDao.getContactsByContactId(contactId)
    }


    suspend fun deleteContact(contact: Contact) {
        contactDao.delete(contact)
    }

}
