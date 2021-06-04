# FlowCache
FlowCache is a wrapper around a kotlinx.coroutines [Flow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/) which caches data and manages statuses.

### The Problems:

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
In the example above we need to:
1. Show/hide progress during long running operations.
2. Handle errors to prevent runtime crashes.
3. Change current ui state after successful result.
4. Somehow handle action duplication. Imagine a situation when users are obsessively pulling to refresh, the code above will be executed many times.

### Download:
```kotlin
allprojects {
  repositories {
    mavenCentral()
  }
}

dependencies {
    implementation 'io.github.onreg:flow-cache:1.0.0'
}
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

Next we can handle it in our Activity/Fragment:

```kotlin
    viewModel.message
        .cache
        .onEach {
            when (it) {
                is Status.Data -> {
                    //show data
                }
                is Status.Error -> {
                    //handle error
                }
                Status.Loading -> {
                    //show progress
                }
            }
        }
        .launchIn(lifecycleScope)
```
No more boilerplate code and we have status/error handling on the fly.

#### The FlowCache provides 4 delegates for ViewModel:

Simple caching, without handling statuses, useful if your repository/data source already provides status handling.

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

The `statusCache` response is a `Flow` of `Status`. `Status` is a Kotlin sealed class that can be either a `Data`, `Loading`, `Error` or `Empty` instance.
1. `Status.Data` - has a `value` field with the data.
2. `Status.Error` - has a `value` field with `Throwable`.
3. `Status.Loading` - indicates that execution has started.
4. `Status.Empty` - fires immediately after each subscription or when `flow` completes without emitting any elements.

#### One shot events

There are some situations when data should be consumed only once, for instance, navigate to another screen after processing a request or show `snackbar` with an error. `Status.Data` and `Status.Error` tracks if data was consumed or not:

```kotlin
    viewModel.message
            .cache
            .onEach {
                when (it) {
                    is Status.Error -> {
                        if (!it.consumed){
                            showError(it.value)
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
```

`FlowCache` provides 3 extensions to get rid of repeating checks:
- `asEvent()`  - filters consumed data
- `asDataEvent()` - filters `Status.Data` and consumed
- `asErrorEvent()` - filters `Status.Error` and consumed

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

To rerun body of `cache<String, String>` you can use `run()` or `run(params)`. In case of `run()` it will use previous params if they exists:

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

Under the hood FlowCache prevents duplication requests with the same parameter, so we don't need to worry about enabling/disabling the user interface. But if the parameter is changed, the previous request will be canceled.

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

## Licence

    Copyright 2021 Vadzim Korzun

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.