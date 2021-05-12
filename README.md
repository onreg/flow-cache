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
Simple caching, without handling statuses, useful if your repository/data source already provides status handling for instance [Store](https://github.com/dropbox/Store).
Params: `start` if `true`, execution will start immediately after first subscriber.

```kotlin
	 val message by cache<String> {  
	 }
 ```

The same as `cache` but with parameter.

```kotlin
	 val message by cache<String, String> { id ->
	 }
 ```

Caching as well as status handling.
Params: `start` and `initialParam`, execution will start immediately after first subscriber if `start` is `true` and `initialParam` isn't `null`.

```kotlin
	 val message by statusCache<String> { 
	 }
 ```

The same as `statusCache` but with parameter.

```kotlin
	val message by statusCache<String, String> { id -> 
	}
```

#### Refresh
It is a common situation to refresh outdated data or repeat request in case of an error.

To rerun body of `cache` you can use `run()`:

```kotlin
    val message by cache<String> {
    }

    fun refresh() {
        message.run()
    }
```

To rerun body of `cache<String, String>` you can use `run()` or `run(params)`. In case of `run()` it will use previous params if it exists:

```kotlin
    val message by cache<String, String> {
    }

    fun refresh() {
        message.run()
    }

    fun refresh(param: String) {
        message.run(param)
    }
```

#### Debounce requests

Under the hood FlowCache prevents duplication requests with the same data, so we don't need to worry about enabling/disabling the user interface. But if the data is changed, the previous request will be canceled.

```kotlin
    fun refresh() {
        message.run("5")
      
        //will be ignored
        message.run("5")
    }
```

```kotlin
    fun refresh() {
        //will be canceled
        message.run("5")
      
        message.run("10")
    }
```