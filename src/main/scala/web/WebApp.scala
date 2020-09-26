package web

import web.session.SessionsApi

class WebApp(
    val sessionsApi: SessionsApi,
    val roomApp: RoomApp,
    val shogiApp: ShogiMock
)
