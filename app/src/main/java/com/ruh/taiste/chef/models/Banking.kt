package com.ruh.taiste.chef.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BusinessBankingInfo(
    val stripeId: String,
    val termsOfServiceAccept: String,
    val mcc: String,
    val name: String,
    val url: String,
    val streetAddress: String,
    val streetAddress2: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val phone: String,
    val companyTaxId: String,
    var documentId: String,
    var externalAccount: BankAccount?,
    var representative: Person?,
    var owner1: Person?,
    var owner2: Person?,
    var owner3: Person?,
    var owner4: Person?

) : Parcelable

@Parcelize
data class BankAccount(
    val bankName: String,
    val accountHolder: String,
    val accountNumber: String,
    val routingNumber: String,
    var externalAccountId: String,
) : Parcelable

@Parcelize
data class Person(
    var isPersonAnExecutive: String,
    val isPersonAnOwner: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val dobDay: String,
    val dobMonth: String,
    val dobYear: String,
    val ssn: String,
    val streetAddress: String,
    val streetAddress2: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val executive: String,
    val owner: String,
    val personId: String,
) : Parcelable

data class Cancelled(
    val documentId: String,
    val amount: Number
)

