package com.homefirstindia.hfo.repository.v1

import com.homefirstindia.hfo.dto.v1.*
import com.homefirstindia.hfo.helper.v1.CommunicationSearchQueryCriteria
import com.homefirstindia.hfo.model.v1.CallLog
import com.homefirstindia.hfo.model.v1.CallingInfo
import com.homefirstindia.hfo.model.v1.SMSLog
import com.homefirstindia.hfo.model.v1.SMSTemplate
import com.homefirstindia.hfo.dto.v1.CallLogExport
import com.homefirstindia.hfo.dto.v1.SMSStats
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.persistence.NoResultException
import javax.persistence.PersistenceContext
import javax.persistence.criteria.*

@Component
@Transactional
class CommunicationRepository(
    @Autowired val smsLogRepository: SMSLogRepository,
    @Autowired val smsTemplateRepository: SMSTemplateRepository,
    @Autowired val callLogRepository: CallLogRepository,
    @Autowired val callingInfoRepository: CallingInfoRepository,
    @PersistenceContext val entityManager: EntityManager,
) {
    @Transactional
    fun findSMSCampaignStats(
        ids: ArrayList<String>?,
    ): List<SMSStats>? {

        return try {

//            val query= StringBuilder()
//            query.append(" SELECT NEW com.homefirstindia.hfo.dto.v1.SMSStats( pd.smsCampaignId, ")
//            query.append(" sum(case when pd.taskStatus = 'DELIVRD' then 1 else 0 end) as success, ")
//            query.append(" sum(case when pd.taskStatus != 'DELIVRD' then 1 else 0 end) as failed, ")
//            query.append(" count(pd.id) as total ) ")
//            query.append(" from (select s.id, s.smsCampaignId, min(s.status) as taskStatus ")
//            query.append(" from SMSLog s")
//            query.append(" where s.smsCampaignId in (:ids) ")
//            query.append(" group by s.id ")
//            query.append(" ) pd ")
//            query.append(" group by pd.smsCampaignId ")
//
//            println("query is :: $query")

            val stats = entityManager.createQuery(
//                query.toString(),
//                " SELECT NEW com.homefirstindia.hfo.dto.v1.SMSStats( pd.smsCampaignId, " +
//                        "       sum(case when pd.taskStatus = 'DELIVRD' then 1 else 0 end) as success, " +
//                        "       sum(case when pd.taskStatus != 'DELIVRD' then 1 else 0 end) as failed, " +
//                        "       count(pd.id) as total ) " +
//                        " from (select s.id, s.smsCampaignId, min(s.status) as taskStatus " +
//                        "      from SMSLog s" +
//                        "      where s.smsCampaignId in (:ids) " +
//                        "      group by s.id " +
//                        "     ) pd " +
//                        " group by pd.smsCampaignId ",

                "SELECT NEW com.homefirstindia.hfo.dto.v1.SMSStats(s.smsCampaignId, " +
                        "SUM(CASE WHEN s.status = 'DELIVRD' THEN 1 ELSE 0 END) as success, " +
                        "SUM(CASE WHEN s.status != 'DELIVRD' THEN 1 ELSE 0 END) as failed, " +
                        "COUNT(s.id) as total) FROM SMSLog s WHERE s.smsCampaignId IN :ids " +
                        "GROUP BY s.smsCampaignId",
                SMSStats::class.java
            )
//                .setParameter("ids", ids)
////                .setMaxResults(1)
//                .singleResult

            stats.setParameter("ids", ids).resultList

        } catch (e: NoResultException) {
            print("getLastLeadAssignedUser - No campaign stats found for ids : $ids ")
            null
        }

    }
}

@Component
@Transactional
class CallLogRepositoryMasterRepository(
    @PersistenceContext val entityManager: EntityManager,
) : CallLogRepositoryCustom {
    @Throws(Exception::class)
    override fun advancedList(
        advanceFilter: AdvanceFilter,
        page: Pageable,
    ): Page<CallLog> {
        val builder = entityManager.criteriaBuilder
        val query = builder.createQuery(CallLogDTO::class.java)
        val r: Root<*> = query.from(CallLog::class.java)

        var pageSize: Int? = 0
        val currentPage: Int?
        var startItem: Int? = 0

        if (page.isPaged) {
            pageSize = page.pageSize
            currentPage = page.pageNumber
            startItem = currentPage * pageSize
        }

        val value = getCollectionPredicate(
            advanceFilter,
            builder,
            query,
            r,
        )
        getCallLogQuery(
            query,
            builder,
            r,
        )

        query.where(value.searchCollection?.predicate)
        query.orderBy(builder.desc(r.get<CallLog>("createDatetime")))

        val callLogs = entityManager.createQuery(query)

        query.select(builder.count(r) as Selection<out Nothing>)

        val qb = entityManager.criteriaBuilder
        val cq = qb.createQuery(Long::class.java)
        val cRoot = cq.from(CallLog::class.java)
        cq.select(qb.count(cRoot))
        cq.where(value.searchCollection?.predicate)

        val count = entityManager.createQuery(cq).singleResult

        if (page.isPaged) {
            callLogs.firstResult = startItem!!
            callLogs.maxResults = pageSize!!
        }

        val callLogsToShow = callLogs.resultList as ArrayList<CallLog>

        return PageImpl(callLogsToShow, page, count as Long)

    }

    private fun getCallLogQuery(
        query: CriteriaQuery<*>,
        builder: CriteriaBuilder,
        r: Root<*>,
    ) {
        query.select(
            builder.construct(
                CallLogDTO::class.java,
                r.get<CallLog>("id"),
                r.get<CallLog>("createDatetime"),
                r.get<CallLog>("type"),
                r.get<CallLog>("callStartTime"),
                r.get<CallLog>("callEndTime"),
                r.get<CallLog>("callerProvider"),
                r.get<CallLog>("callerStatus"),
                r.get<CallLog>("receiverStatus"),
                r.get<CallLog>("receiver"),
                r.get<CallLog>("caller"),
                r.get<CallLog>("callerLocation"),
                r.get<CallLog>("source"),
                r.get<CallLog>("status"),
                r.get<CallLog>("updateDatetime"),
                r.get<CallLog>("userName"),
                r.get<CallLog>("objectId"),
                r.get<CallLog>("objectName"),
                r.get<CallLog>("userId"),
                r.get<CallLog>("durationInSec"),
                r.get<CallLog>("remark")
            ) as Selection<out Nothing>
        )
    }

    private fun getCollectionPredicate(
        advanceFilter: AdvanceFilter,
        builder: CriteriaBuilder,
        query: CriteriaQuery<*>,
        r: Root<*>,
    ): CallLogList {

        val predicate: Predicate = if (advanceFilter.groupConditionOp == "And") {
            builder.conjunction()
        } else {
            builder.disjunction()
        }

        val searchCallLog = CommunicationSearchQueryCriteria(
            predicate,
            builder,
            r,
            advanceFilter.groupConditionOp!!
        )

        advanceFilter.conditions.stream().forEach(searchCallLog)

        return CallLogList(
            advanceFilter,
            builder,
            query,
            r,
            searchCallLog,
        )
    }
}


@Repository
interface SMSLogRepository : JpaRepository<SMSLog, String> {

//    @Query("select s from SMSLog s where" :type ""= :objectId " +
//            " order by s.createDatetime desc")
//    fun findAllSMSLogs(objectId: String, type: String, page: Pageable): Page<SMSLog>?


    fun findAllBySmsCampaignId(smsCampaignId: String, page: Pageable): Page<SMSLog>?

    @Query("FROM SMSLog s WHERE s.smsCampaignId = :smsCampaignId and (s.status != 'DELIVRD' and s.status != 'AWAITED-DLR')")
    fun findBySmsCampaignId(smsCampaignId: String, page: Pageable): Page<SMSLog>?

    fun findAllByObjectId(objectId: String, page: Pageable): Page<SMSLog>?

//
//    @Query(" select new com.homefirstindia.hfo.dto.v1.SMSStats( pd.smsCampaignId, " +
//            " sum(case when taskStatus = 'DELIVRD' then 1 else 0 end) as success, " +
//            "       sum(case when taskStatus != 'DELIVRD' then 1 else 0 end) as failed, " +
//            "       count(*) as total ) " +
//            " from (select s.id, s.smsCampaignId, min(s.status) as taskStatus " +
//            "      from SMSLog s " +
//            "      where s.smsCampaignId in :smsCampaignId " +
//            "      group by s.id " +
//            "     ) pd  " +
//            " group by pd.smsCampaignId", nativeQuery = true )
////    @Query("SELECT NEW com.homefirstindia.hfo.dto.v1.smsC ountDTO(s.smsCampaignId, s.status, count(s.status) ) " +
////            "FROM SMSLog s WHERE " +
////            "s.smsCampaignId in (:smsCampaignId) group by s.status ", nativeQuery = true)
//    fun findSMSCampaignStats(smsCampaignId: ArrayList<String>) : ArrayList<SMSStats>?

    fun findBySmsId(id: String): SMSLog?

}

@Repository
interface CallLogRepository : JpaRepository<CallLog, String> {



    @Query("from CallLog as c where c.callId = :callId")
    fun findCallLogByCallId(callId: String?): CallLog?

    fun findAllByObjectId(objectId: String, page: Pageable): Page<CallLog>?

    @Query(
        "SELECT new com.homefirstindia.hfo.dto.v1.CallLogExport(c.id," +
                " c.objectId, c.source, c.userName," +
                " c.userEmail, c.callerStatus, c.receiverStatus, c.createDatetime, c.remark)" +
                " FROM CallLog c" +
                " WHERE c.createDatetime >= :startDatetime AND c.createDatetime <= :endDatetime" +
                " AND c.objectName = :objectName" +
                " ORDER BY c.createDatetime DESC"
    )
    fun findForExport(startDatetime: String?, endDatetime: String?, objectName: String): ArrayList<CallLogExport>?

}

@Repository
interface CallingInfoRepository : JpaRepository<CallingInfo, String> {

    fun findByCallerIdAndIsActiveTrue(callerId: String?): CallingInfo?

    @Query("from CallingInfo where description = 'Homefirst' and isActive = true")
    fun findDefaultCallingInfo(): CallingInfo?
}

@Repository
interface SMSTemplateRepository : JpaRepository<SMSTemplate, String>

interface CallLogRepositoryCustom {
    fun advancedList(
        advanceFilter: AdvanceFilter,
        page: Pageable,
    ): Page<CallLog>?

}

