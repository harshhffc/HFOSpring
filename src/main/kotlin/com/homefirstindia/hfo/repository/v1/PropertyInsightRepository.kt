package com.homefirstindia.hfo.repository.v1

import com.homefirstindia.hfo.model.v1.PropertyInsight
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PropertyInsightRepository : JpaRepository<PropertyInsight, String> {

    fun findBySfPropertyInsightId(sfPropertyInsightId: String?): PropertyInsight?

    fun findByQueryId(queryId: String?): PropertyInsight?

    fun findBySfPropertyInsightIdAndStatus(sfPropertyInsightId: String?, status: String?): PropertyInsight?

    fun findBySfPropertyInsightIdAndStatusIn(sfPropertyInsightId: String?, status: ArrayList<String>?): PropertyInsight?

}

