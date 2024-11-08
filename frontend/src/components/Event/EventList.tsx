import * as l from '../style/common/ListCardStyle';
import EventItem from './EventItem';
import { useQuery } from '@tanstack/react-query';
import { GetEventApi } from '../../apis/EventApis';
import Loading from '../common/Loading/Loading';

const EventList = () => {
  const {
    isLoading: isEventLoading,
    data: eventsData,
  } = useQuery({
    queryKey: ['get-events'],
    queryFn: () => GetEventApi(),
  });

  if (isEventLoading) {
    return <Loading />;
  }

  return (
    <l.Wrapper>
      {eventsData?.data.map((event: eventApitype, index: string) => (
        <l.EventCard key={index}>
          <EventItem event={event} />
        </l.EventCard>
      ))}
    </l.Wrapper>
  );
};

export default EventList;
