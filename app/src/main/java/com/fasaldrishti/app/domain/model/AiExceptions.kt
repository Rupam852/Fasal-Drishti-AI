package com.fasaldrishti.app.domain.model

class AiApiKeyException(
    message: String = "Aapki Custom AI Key expire ya galat ho sakti hai. Kripya Settings ➔ AI Engine me jakar nayi key dalein ya Default Engine chunein."
) : Exception(message)

class AiUnreachableException(
    message: String = "Unable to connect to AI engines due to network or quota limit. Please try again shortly."
) : Exception(message)
