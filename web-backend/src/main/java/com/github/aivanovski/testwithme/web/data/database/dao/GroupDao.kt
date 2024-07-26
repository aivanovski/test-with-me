package com.github.aivanovski.testwithme.web.data.database.dao

import com.github.aivanovski.testwithme.web.data.database.AppDatabase
import com.github.aivanovski.testwithme.web.entity.Uid
import com.github.aivanovski.testwithme.web.entity.Group

class GroupDao(
    db: AppDatabase
): Dao<Group>(
    db = db,
    entityType = Group::class.java,
    entityName = "Group"
) {

    fun getByProjectUid(projectUid: Uid): List<Group> {
        val variableName = entityName.uppercase().first()
        return db.execTransaction {
            createQuery(
                "From $entityName $variableName WHERE $variableName.projectUid = :uid",
                entityType
            )
                .setParameter("uid", projectUid)
                .resultList
        }
    }
}