import { useState } from "react";
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from "@fullcalendar/interaction";
import luxonPlugin from "@fullcalendar/luxon3";
import ptBr from "@fullcalendar/core/locales/pt-br";
import { DateTime } from "luxon";
import { useNavigate } from "react-router-dom";
import { useApi } from "../hooks/useApi";
import { useApp } from "../app/context";
import { PageTitle, ErrorState, Modal } from "../components/ui";
import { ResourceForm } from "../features/resources/ResourceForm";
import { ResourcePage } from "../features/resources/ResourcePage";
import { eventsConfig } from "../features/resources/config";
import { ZONE, toInputDateTime, contrastText } from "../services/dates";
import { query } from "../services/api";
import type { CalendarItem, CalendarEvent } from "../types/api";
export function CalendarPage() {
  const navigate = useNavigate();
  const { subjects } = useApp();
  const [range, setRange] = useState({
    from: DateTime.now()
      .setZone(ZONE)
      .startOf("month")
      .minus({ days: 7 })
      .toUTC()
      .toISO()!,
    to: DateTime.now()
      .setZone(ZONE)
      .endOf("month")
      .plus({ days: 7 })
      .toUTC()
      .toISO()!,
  });
  const result = useApi<CalendarItem[]>("/calendar" + query(range));
  const [editId, setEditId] = useState<number>();
  const [newDate, setNewDate] = useState<string>();
  const [revision, setRevision] = useState(0);
  function refresh() {
    setNewDate(undefined);
    setEditId(undefined);
    setRevision((v) => v + 1);
    result.reload();
  }
  return (
    <>
      <PageTitle
        title="Calendário de estudos"
        description="Dê espaço aos estudos, às avaliações e à vida. Horários de São Paulo."
      />
      <section className="card calendar-card">
        {result.error && (
          <ErrorState message={result.error} retry={result.reload} />
        )}
        <div className="calendar-legend">
          <span>
            <i className="legend-study" />
            Estudo
          </span>
          <span>
            <i className="legend-exam" />
            Prova
          </span>
          <span>
            <i className="legend-personal" />
            Pessoal
          </span>
          {result.loading && <span role="status">Atualizando…</span>}
        </div>
        <FullCalendar
          plugins={[
            dayGridPlugin,
            timeGridPlugin,
            interactionPlugin,
            luxonPlugin,
          ]}
          locale={ptBr}
          timeZone={ZONE}
          initialView="dayGridMonth"
          headerToolbar={{
            left: "prev,next today",
            center: "title",
            right: "dayGridMonth,timeGridWeek,timeGridDay",
          }}
          buttonText={{
            today: "Hoje",
            month: "Mês",
            week: "Semana",
            day: "Dia",
          }}
          height="auto"
          firstDay={1}
          nowIndicator
          selectable={false}
          allDaySlot={false}
          eventTimeFormat={{
            hour: "2-digit",
            minute: "2-digit",
            hour12: false,
          }}
          events={(result.data || []).map((e) => ({
            id: e.id,
            title: e.title,
            start: e.startAt,
            end: e.endAt,
            backgroundColor:
              e.type === "EXAM"
                ? "#9A6B42"
                : e.type === "PERSONAL"
                  ? "#64766C"
                  : subjects.find((s) => s.id === e.subjectId)?.color ||
                    "#527C68",
            textColor: contrastText(
              e.type === "EXAM"
                ? "#9A6B42"
                : e.type === "PERSONAL"
                  ? "#64766C"
                  : subjects.find((s) => s.id === e.subjectId)?.color ||
                    "#527C68",
            ),
            borderColor: "transparent",
            extendedProps: { source: e.source, sourceId: e.sourceId },
          }))}
          datesSet={(arg) =>
            setRange({
              from: arg.start.toISOString(),
              to: arg.end.toISOString(),
            })
          }
          dateClick={(arg) =>
            setNewDate(toInputDateTime(arg.date.toISOString()))
          }
          eventClick={(arg) => {
            if (arg.event.extendedProps.source === "EXAM") navigate("/exams");
            else setEditId(Number(arg.event.extendedProps.sourceId));
          }}
        />
      </section>
      <ResourcePage<CalendarEvent>
        key={revision}
        config={eventsConfig}
        embedded
        externalEditId={editId}
        onEdited={refresh}
      />
      {newDate && (
        <Modal title="Novo evento" onClose={() => setNewDate(undefined)}>
          <ResourceForm
            config={eventsConfig}
            initial={{
              startAt: newDate,
              endAt: DateTime.fromISO(newDate, { zone: ZONE })
                .plus({ hours: 1 })
                .toFormat("yyyy-MM-dd'T'HH:mm"),
            }}
            onSaved={refresh}
            onCancel={() => setNewDate(undefined)}
          />
        </Modal>
      )}
    </>
  );
}
