package com.homefirstindia.hfo.repository.v1

import com.homefirstindia.hfo.model.v1.Attachment
import com.homefirstindia.hfo.model.v1.DocumentJunction
import com.homefirstindia.hfo.model.v1.KYCDocument
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class DocumentRepositoryMaster(
    @Autowired val documentJunctionRepository: DocumentJunctionRepository,
    @Autowired val attachmentRepository: AttachmentRepository,
    @Autowired val kycDocumentRepository: KYCDocumentRepository
)

interface DocumentJunctionRepository : JpaRepository<DocumentJunction, String> {

    @Query("from DocumentJunction where objectId = :objectId")
    fun getAllByObjectId(objectId: String?): ArrayList<DocumentJunction>?

}

@Repository
interface AttachmentRepository : JpaRepository<Attachment, String> {

    fun getAttachmentByObjectId(objectId: String?): ArrayList<Attachment>?

    @Query("from Attachment where fileIdentifier = :fid")
    fun findAttachmentByFid(fid: String): Attachment?

}


@Repository
interface KYCDocumentRepository : JpaRepository<KYCDocument, String> {

    fun findByDocumentIdAndDocumentType(documentId: String?, documentType: String?): KYCDocument?

}
