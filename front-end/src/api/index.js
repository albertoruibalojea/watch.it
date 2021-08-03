import DATA from './data'

let __instance = null

export default class API {
    #token = sessionStorage.getItem('token') || null

    static instance() {
        if(__instance == null)
            __instance = new API()

        return __instance
    }

    async login(email, pass) {
        //First, we create the JSON Object to send it to the API in the body
        var JSONObj = { "email" : email, "password"  : pass }

        // TODO fetch from API and if successful, store token from response headers
        //const user = DATA.users.find(u => u.email === email)
        const response = await fetch("http://localhost:8080/api/login", {
            method: 'POST', // *GET, POST, PUT, DELETE, etc
            mode: 'cors', // no-cors, *cors, same-origin
            cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
            credentials: 'same-origin', // include, *same-origin, omit
            headers: {
                'Content-Type': 'application/json'
                //'Authorization': this.#token
            },
            redirect: 'follow', // manual, *follow, error
            referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
            body: JSON.stringify(JSONObj) // body data type must match "Content-Type" header
        });

        if(response.ok) {
            localStorage.setItem('user', email)
            localStorage.setItem('token', response.headers.get('Authentication'))
            this.#token = response.headers.get('Authentication')

            return true
        } else {
            return false
        }
    }
    async logout() {
        this.#token = null
        localStorage.clear()

        return true
    }

    async findMovies(
        {
            filter: { genre = '', title = '', status = '' } = { genre : '', title : '', status : '' },
            sort,
            pagination: {page = 0, size = 7} = { page: 0, size: 7 }
        } = {
            filter: { genre : '', title : '', status : '' },
            sort: {},
            pagination: { page: 0, size: 7 }
        }
    ) {

        var url = ''
        if(genre !== '') url ='http://localhost:8080/api/films?genres='+genre
        else if(genre === '' && title === '') url = 'http://localhost:8080/api/films?'
        else if(title !== '') url = 'http://localhost:8080/api/films?title='+title

        //?page='+page+'&size='+size+'&genre='+genre+'&title='+title
        const response = await fetch(url, {
            method: 'GET', // *GET, POST, PUT, DELETE, etc
            mode: 'cors', // no-cors, *cors, same-origin
            cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
            credentials: 'same-origin', // include, *same-origin, omit
            headers: {
                'Content-Type': 'application/json',
                Authorization: localStorage.getItem('token')
            },
            redirect: 'follow', // manual, *follow, error
            referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
        }).then(response => response.json());

        return new Promise(resolve => {
            const data = {
                content: response.content?.slice(size * page, size * page + size),
                pagination: {
                    hasNext: size * page + size < response.length,
                    hasPrevious: page > 0
                }
            }

            resolve(data)
        })
    }
    async findMovie(id) {
        //return DATA.movies.find(movie => movie.id === id)
        const response = await fetch('http://localhost:8080/api/films/'+id, {
            method: 'GET', // *GET, POST, PUT, DELETE, etc
            mode: 'cors', // no-cors, *cors, same-origin
            cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
            credentials: 'same-origin', // include, *same-origin, omit
            headers: {
                'Content-Type': 'application/json',
                Authorization: localStorage.getItem('token')
            },
            redirect: 'follow', // manual, *follow, error
            referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
        });

        if(response.ok) {
            const body = await response.json()
            return body
        }
    }
    async findUser(id) {
        return new Promise(resolve => {
            const user = fetch('http://localhost:8080/api/users/'+id, {
                method: 'GET', // *GET, POST, PUT, DELETE, etc
                mode: 'cors', // no-cors, *cors, same-origin
                cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
                credentials: 'same-origin', // include, *same-origin, omit
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: localStorage.getItem('token')
                },
                redirect: 'follow', // manual, *follow, error
                referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
            });

            resolve(user)
        })
    }
    async findComments(
        {
            filter: { movie = '', user = '' } = { movie: '', user: '' },
            sort,
            pagination: {page = 0, size = 10} = { page: 0, size: 10}
        } = {
            filter: { movie: '', user: '' },
            sort: {},
            pagination: { page: 0, size: 10}
        }
    ) {
        var url = ''
        if(movie !== '' && user === '') url ='http://localhost:8080/api/films/'+movie+'/assessments'
        else if(movie === '' && user !== '') url ='http://localhost:8080/api/users/'+user+'/assessments'

        const response = await fetch(url, {
            method: 'GET', // *GET, POST, PUT, DELETE, etc
            mode: 'cors', // no-cors, *cors, same-origin
            cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
            credentials: 'same-origin', // include, *same-origin, omit
            headers: {
                'Content-Type': 'application/json',
                Authorization: localStorage.getItem('token')
            },
            redirect: 'follow', // manual, *follow, error
            referrerPolicy: 'no-referrer' // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
        }).then(response => response.json());

        return new Promise(resolve => {
            const data = {
                content: response.content?.slice(size * page, size * page + size),
                pagination: {
                    hasNext: size * page + size < response.content.length,
                    hasPrevious: page > 0
                }
            }

            resolve(data)
        })
    }

    async createMovie(movie) {

        let genres = []
        movie.genres.forEach((item, index) => {
            genres.push(item)
        })

        let keywords = []
        movie.keywords.forEach((item, index) => {
            keywords.push(item)
        })

        let producers = []
        movie.producers.forEach((item, index) => {
            producers.push("{'name': " + item.name + "'logo': " + item.logo + "'country': " + item.country + "}")
        })

        let crew = []
        movie.crew.forEach((item, index) => {
            crew.push("{'job': " + item.job + "'name': " + item.name + "'picture': " + item.picture + "}")
        })

        let cast = []
        movie.cast.forEach((item, index) => {
            cast.push("{'character': " + item.character + "'name': " + item.name + "'picture': " + item.picture + "}")
        })

        let resources = []
        movie.resources.forEach((item, index) => {
            resources.push("{'url': " + item.url + "'type': " + item.type + "}")
        })


        var JSONObj = {
            "title": movie.title,
            "overview": movie.overview,
            "tagline": movie.tagline,
            "genres": genres,
            "releaseDate": {
                "day": movie.releaseDate.day,
                "month": movie.releaseDate.month,
                "year": movie.releaseDate.year
            },
            "keywords": keywords,
            "producers": producers,
            "crew": crew,
            "cast": cast,
            "resources": resources,
            "budget": movie.budget,
            "status": movie.status
        }


        return new Promise(resolve => {

            const response = fetch('http://localhost:8080/api/films/', {
                method: 'POST', // *GET, POST, PUT, DELETE, etc
                mode: 'cors', // no-cors, *cors, same-origin
                cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
                credentials: 'same-origin', // include, *same-origin, omit
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: localStorage.getItem('token')
                },
                redirect: 'follow', // manual, *follow, error
                referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
                body: JSON.stringify(JSONObj)
            });

            resolve(response)
        })
    }
    async createComment(comment) {
        var JSONObj = {
            "user": {
                "email": comment.user.email
            },
            "film": {
                "id": comment.film.id
            },
            "comment": comment.comment,
            "rating": comment.rating
        }

        return new Promise(resolve => {

            const response = fetch('http://localhost:8080/api/assessments/', {
                method: 'POST', // *GET, POST, PUT, DELETE, etc
                mode: 'cors', // no-cors, *cors, same-origin
                cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
                credentials: 'same-origin', // include, *same-origin, omit
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: localStorage.getItem('token')
                },
                redirect: 'follow', // manual, *follow, error
                referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
                body: JSON.stringify(JSONObj)
            });

            resolve(response)
        })
    }
    async createUser(user) {

        var JSONObj = {
            "email": user.email,
            "name": user.name,
            "country": user.country,
            "picture": user.picture,
            "password": user.password,
            "roles": [
                "ROLE_USER"
            ],
            "birthday": {
                "day": user.birthday.day,
                "month": user.birthday.month,
                "year": user.birthday.year
            }
        }

        return new Promise(resolve => {

            const response = fetch('http://localhost:8080/api/users/', {
                method: 'POST', // *GET, POST, PUT, DELETE, etc
                mode: 'cors', // no-cors, *cors, same-origin
                cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
                credentials: 'same-origin', // include, *same-origin, omit
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: localStorage.getItem('token')
                },
                redirect: 'follow', // manual, *follow, error
                referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
                body: JSON.stringify(JSONObj)
            });

            resolve(response)
        })
    }

    async updateMovie(id, movie) {
        let JSONObj = ""
        console.log(id)
        console.log(movie)

        const actualMovie = this.findMovie(id).then((response) => {
            if(response.overview !== movie.overview){
                JSONObj = [
                    { "op": "replace", "path": "/overview", "value": movie.overview }
                ]
            }

            if(response.resources !== movie.resources){
                const movieLen = movie.resources.length
                const newResource = movie.resources[movieLen-1]
                if(newResource.type === 'TRAILER'){
                    response.resources.forEach((item, index) => {
                        if(item.type === 'TRAILER'){
                            JSONObj = [
                                { "op": "replace", "path": "/resources/"+index, "value": {
                                        "type": newResource.type,
                                        "url": newResource.url
                                    } }
                            ]
                        }
                    })
                } else {
                    JSONObj = [
                        { "op": "add", "path": "/resources/-", "value": {
                                "type": newResource.type,
                                "url": newResource.url
                            } }
                    ]
                }


                console.log(JSONObj)
            }

            if(JSONObj.length > 0) {
                return new Promise(resolve => {
                    const response = fetch('http://localhost:8080/api/films/' + id, {
                        method: 'PATCH', // *GET, POST, PUT, DELETE, etc
                        mode: 'cors', // no-cors, *cors, same-origin
                        cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
                        credentials: 'same-origin', // include, *same-origin, omit
                        headers: {
                            'Content-Type': 'application/json-patch+json',
                            Authorization: localStorage.getItem('token')
                        },
                        redirect: 'follow', // manual, *follow, error
                        referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
                        body: JSON.stringify(JSONObj)
                    });

                    resolve(response)
                })
            }
        })
    }
    async updateUser(id, user) {

        let JSONObj = []

        const actualUser = this.findUser(id)
        if((actualUser.email === user.email) && (actualUser.birthday === user.birthday)){
            if(actualUser.name !== user.name){
                JSONObj = [
                    { "op": "replace", "path": "/name", "value": user.name }
                ]
            }
            if(actualUser.country !== user.country){
                JSONObj = [
                    { "op": "replace", "path": "/name", "value": user.country }
                ]
            }
            if(actualUser.picture !== user.picture){
                JSONObj = [
                    { "op": "replace", "path": "/name", "value": user.picture }
                ]
            }
            if(actualUser.password !== user.password){
                JSONObj = [
                    { "op": "replace", "path": "/name", "value": user.password }
                ]
            }
        }

        if(JSONObj.length > 0) {
            return new Promise(resolve => {
                const response = fetch('http://localhost:8080/api/users/' + id, {
                    method: 'PATCH', // *GET, POST, PUT, DELETE, etc
                    mode: 'cors', // no-cors, *cors, same-origin
                    cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
                    credentials: 'same-origin', // include, *same-origin, omit
                    headers: {
                        'Content-Type': 'application/json-patch+json',
                        Authorization: localStorage.getItem('token')
                    },
                    redirect: 'follow', // manual, *follow, error
                    referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
                    body: JSON.stringify(JSONObj)
                });

                resolve(response)
            })
        }
    }
}