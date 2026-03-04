package com.huanchengfly.tieba.post.utils

import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.api.models.MessageListBean
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.SubPostList
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.abstractText
import com.huanchengfly.tieba.post.api.models.protos.plainText
import com.huanchengfly.tieba.post.models.database.Block
import com.huanchengfly.tieba.post.models.database.Block.Companion.getKeywords
import com.huanchengfly.tieba.post.models.database.dao.BlockDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object BlockManager {
    private val blockList: MutableList<Block> = mutableListOf()
    private val blockListLock = Any()

    lateinit var blockDao: BlockDao
        private set

    fun initDao(dao: BlockDao) {
        blockDao = dao
    }

    val blackList: List<Block>
        get() = getBlockSnapshot().filter { it.category == Block.CATEGORY_BLACK_LIST }

    val whiteList: List<Block>
        get() = getBlockSnapshot().filter { it.category == Block.CATEGORY_WHITE_LIST }

    suspend fun addBlock(block: Block) {
        blockDao.insert(block)
        synchronized(blockListLock) {
            blockList.add(block)
        }
    }

    fun addBlockAsync(
        block: Block,
        callback: ((Boolean) -> Unit)? = null,
    ) {
        App.appScope.launch(Dispatchers.IO) {
            runCatching {
                addBlock(block)
                callback?.invoke(true)
            }.onFailure {
                callback?.invoke(false)
            }
        }
    }

    suspend fun removeBlock(id: Long) {
        blockDao.deleteById(id)
        synchronized(blockListLock) {
            blockList.removeAll { it.id == id }
        }
    }

    suspend fun init() {
        val blocks = blockDao.getAll()
        synchronized(blockListLock) {
            blockList.clear()
            blockList.addAll(blocks)
        }
    }

    private fun getBlockSnapshot(): List<Block> = synchronized(blockListLock) { blockList.toList() }

    fun shouldBlock(content: String): Boolean {
        return blackList.any { block ->
            block.type == Block.TYPE_KEYWORD
                    && block.getKeywords().all { content.contains(it) }
        } && whiteList.none { block ->
            block.type == Block.TYPE_KEYWORD
                    && block.getKeywords().all { content.contains(it) }
        }
    }

    fun shouldBlock(userId: Long = 0L, userName: String? = null): Boolean {
        return blackList.any { block ->
            block.type == Block.TYPE_USER
                    && (block.uid == userId.toString() || block.username == userName)
        } && whiteList.none { block ->
            block.type == Block.TYPE_USER
                    && (block.uid == userId.toString() || block.username == userName)
        }
    }

    fun ThreadInfo.shouldBlock(): Boolean =
        shouldBlock(title) || shouldBlock(abstractText) || shouldBlock(authorId, author?.name)

    fun Post.shouldBlock(): Boolean =
        shouldBlock(content.plainText) || shouldBlock(author_id, author?.name)

    fun SubPostList.shouldBlock(): Boolean =
        shouldBlock(content.plainText) || shouldBlock(author_id, author?.name)

    fun MessageListBean.MessageInfoBean.shouldBlock(): Boolean =
        shouldBlock(content.orEmpty()) || shouldBlock(
            this.replyer?.id?.toLongOrNull() ?: -1,
            this.replyer?.name.orEmpty()
        )
}
