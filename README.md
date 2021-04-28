# FlowCache
FlowCache is a wrapper around a Flow which caches data and manage satuses.

### The Problems:

#### A lot of boilerplate code

Typical ViewModel looks like:

```kotlin
class DetailsViewModel(val api: Api) : ViewModel() {

    private val _progress = MutableStateFlow(false)
    val progress = _progress.asStateFlow()

    private val _error = MutableStateFlow<Throwable?>(null)
    val error = _error.asStateFlow()

    private val _message = MutableStateFlow("")
    val message = _message.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _progress.value = true
            try {
                _message.value = api.getMessage()
            } catch (ex: Exception) {
                _error.value = ex
            }
            _progress.value = false
        }
    }
}
```
And with each action we need to:
1. Show/hide progress during long running operations.
2. Handle errors to prevent runtime crashes.
3. Change current ui state after successful result.

#### Action duplication

Imagine a situation when users are obsessively pulling to refresh, the block of code above will be executed many times.

### Download:
```kotlin
TBD
```

### Usage:

The code snippet above we can simplify:
```kotlin
    val message by statusCache<String> {
        flow {
            emit(api.getMessage())
        }
    }
```

Next we can handle `message` in our Activity/Fragment:
```kotlin
    viewModel.message
        .cache
        .onEach {
            when (it) {
                is Status.Data -> {
                    //Show data
                }
                is Status.Error -> {
                    //Handle error
                }
                Status.Loading -> {
                    //Show progress
                }
            }
        }
        .launchIn(lifecycleScope)
```
That's it we got rid of boilerplate code and we got status/error handling on the fly.

#### The FlowCache provides 4 delegates for ViewModel:

```kotlin
    val message by cache<String> {

    }
```

```kotlin
    val message by cache<String, String> { id ->

    }
```

```kotlin
    val message by statusCache<String> {

    }
```

```kotlin
    val message by statusCache<String, String> { id -> 

    }
```