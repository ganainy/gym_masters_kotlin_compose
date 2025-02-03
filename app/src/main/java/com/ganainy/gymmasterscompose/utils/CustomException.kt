package com.ganainy.gymmasterscompose.utils


class CustomException(val stringRes: Int, exception: Exception=Exception("unknown exception")) : Exception()