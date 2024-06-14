package com.homefirstindia.hfo.repository.v1

import com.homefirstindia.hfo.model.v1.salesforce.ServiceRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

//class ServiceRequestRepository {
//}

@Repository
interface ServiceRequestRepository : JpaRepository<ServiceRequest, String> {


}
