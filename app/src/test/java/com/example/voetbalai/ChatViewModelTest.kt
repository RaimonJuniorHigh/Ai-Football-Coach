package com.example.voetbalai

import com.example.voetbalai.domain.chat.ChatApi
import com.example.voetbalai.domain.chat.ChatRequest
import com.example.voetbalai.domain.chat.ChatRole
import com.example.voetbalai.domain.chat.FakeChatApi
import com.example.voetbalai.viewmodel.chat.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {
    private val repository = FakeTeamRepository()

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun messageGetsReplyBelowConversation() = runTest {
        val vm = ChatViewModel(repository, FakeChatApi(latencyMs = 0))
        vm.onMessageSent("  Maak een training  ")
        val messages = vm.uiState.value.messages
        assertEquals(listOf(ChatRole.USER, ChatRole.ASSISTANT), messages.map { it.role })
        assertEquals("Maak een training", messages.first().text)
        assertFalse(vm.uiState.value.isBusy)
    }

    @Test fun blankMessageIsIgnored() = runTest {
        val vm = ChatViewModel(repository, FakeChatApi(latencyMs = 0))
        vm.onMessageSent("   ")
        assertTrue(vm.uiState.value.messages.isEmpty())
    }

    @Test fun apiReceivesTeamDataAndWholeConversation() = runTest {
        var seen: ChatRequest? = null
        val api = object : ChatApi {
            override suspend fun reply(request: ChatRequest): String {
                seen = request
                return "ok"
            }
        }
        val vm = ChatViewModel(repository, api)
        vm.onMessageSent("hoi")
        assertEquals("JO17-1", seen?.teamName)
        assertEquals(6, seen?.players?.size)
        assertEquals(listOf("hoi"), seen?.messages?.map { it.text })
    }

    @Test fun apiErrorBecomesErrorMessage() = runTest {
        val api = object : ChatApi {
            override suspend fun reply(request: ChatRequest): String = error("geen internet")
        }
        val vm = ChatViewModel(repository, api)
        vm.onMessageSent("hoi")
        assertTrue(vm.uiState.value.errorMessage!!.contains("geen internet"))
        assertFalse(vm.uiState.value.isBusy)
    }

    @Test fun clearingChatEmptiesConversation() = runTest {
        val vm = ChatViewModel(repository, FakeChatApi(latencyMs = 0))
        vm.onMessageSent("hoi")
        vm.onChatCleared()
        assertTrue(vm.uiState.value.messages.isEmpty())
    }
}
