import {Separator, Shell, TODO} from '../../components'
import ReactPlayer from 'react-player'
import { useParams } from 'react-router-dom'
import {useComments, useUser} from "../../hooks";
import {CalendarOutline as Calendar, LocationMarkerOutline as Location} from "@graywolfai/react-heroicons";



export default function Profile() {
    const user = useUser(null).user
    console.log(user)

    return <Shell>
        <img style = {{ height: '25rem' }}
             src = {`${user.picture}`}
             alt = { `${user.picture} backdrop` }
             className = 'absolute top-2 left-0 right-0 w-full object-cover filter blur transform scale-105' />

        <div className = 'mx-auto w-full max-w-screen-2xl p-8'>
            <Header user = { user } />
            <Comments user = { user } />
        </div>

    </Shell>
}
function Header({ user }) {
    console.log(user)

    return <header className = 'mt-64 relative flex items-center pb-8'>
                <img
                    style= {{ width: '25%', position: 'relative', left: '10%', 'z-index': '10' }}
                     src = { `${user.picture}` }
                     alt = { `${user.name} poster` }
                     className = 'rounded-full flex items-center shadow-xl'/>
                <hgroup
                    style= {{ position: 'relative', right: '10%' }}
                    className = 'flex-1'>
                    <h1 className = {`bg-black bg-opacity-25 backdrop-filter backdrop-blur 
                                                  text-right text-white text-6xl font-bold
                                                  p-8`}>
                        { user.name }
                    </h1>
                    <div className="mt-8 ml-72 text-lg">
                        <Calendar class="inline-grid mr-2" style={{width: '5%'}} />
                        <h3 class="inline-grid"> {user.birthday.day}/{user.birthday.month}/{user.birthday.year} </h3>

                        <Location class="inline-grid ml-32 mr-2" style={{width: '5%'}}/>
                        <h3 className="inline-grid"> {user.country} </h3>

                        <h3 className="inline-grid ml-32"> {user.email} </h3>
                    </div>
                </hgroup>
    </header>
}
function Comments({ user }) {
    const { comments, createComment } = useComments({ filter: { user : user.email } } )
    console.log(comments)

    return <>
        <h2 className = 'mt-16 font-bold text-2xl'>Últimos comentarios</h2>
        <Separator />
        <div className="gap-8 w-full ml-48 ajustarComentarios">
            {
                comments.content.map((el) => (
                    <div className="w-2/3 mb-8">
                        <div className="bg-white rounded border border-gray-250 p-8 flex flex-col shadow-xl text-teal-900">
                            <div className="box-border p-1" >
                                <div className = "w-full grid grid-cols-2 gap-5">
                                    <div className="h-8 mt-2 font-bold text-type">
                                        <h6>{el.user.email}</h6>
                                    </div>
                                    <div className="h-8 mt-2 font-bold text-type">
                                        <p className="float-right">Puntuación: {el.rating}/10</p>
                                    </div>
                                </div>
                                <div className="text-sm.block mt-6 text-type text-sm">
                                    <p>{el.comment}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                ))
            }
        </div>
    </>
}