import statsmodels.api as sm

from plotly import graph_objs as go


def _axis_title_font_layout():
    return {'family': 'Courier New, monospace', 'size': 18, 'color': '#7f7f7f'}


def ecdf_plot(series, upper=False, figure_title=None, xaxis_title=None,
              xaxis_type=None, yaxis_type=None, yaxis_range=None,
              yaxis_title=None):

    # generate the data for the figure...
    ecdf = sm.distributions.ECDF(series)
    if upper:
        trace = go.Scatter(x=ecdf.x, y=1 - ecdf.y)
    else:
        trace = go.Scatter(x=ecdf.x, y=ecdf.y)
    data = [trace]

    # create a layout for the figure...
    xaxis_layout = {'title': xaxis_title, 'type': xaxis_type,
                    'titlefont': _axis_title_font_layout()}
    yaxis_layout = {'range': yaxis_range, 'type': yaxis_type,
                    'title': yaxis_title, 'titlefont': _axis_title_font_layout()}
    layout = go.Layout(title=figure_title,
                       xaxis=xaxis_layout,
                       yaxis=yaxis_layout,
                       )

    # combine data and layout to create the figure...
    fig = go.Figure(data=data, layout=layout)

    return fig


def kde_plot(series, upper=False, figure_title=None, xaxis_title=None,
             xaxis_type=None, yaxis_type=None, xaxis_range=None,
             yaxis_title=None, width=800, height=500):

    # generate the data for the figure...
    data = []
    for s in series:
        clean_series = s.dropna()
        kde = sm.nonparametric.KDEUnivariate(clean_series)
        kde.fit(fft=False)
        xs = clean_series.sort_values().values
        trace = go.Scatter(x=xs, y=kde.evaluate(xs))
        data.append(trace)

    # create a layout for the figure...
    xaxis_layout = {'title': xaxis_title, 'range': xaxis_range,
                    'type': xaxis_type, 'titlefont': _axis_title_font_layout()}
    yaxis_layout = {'type': yaxis_type, 'title': 'Density',
                    'titlefont': _axis_title_font_layout()}
    layout = go.Layout(title=figure_title,
                       xaxis=xaxis_layout,
                       yaxis=yaxis_layout,
                       width=width,
                       height=height
                       )

    # combine data and layout to create the figure...
    fig = go.Figure(data=data, layout=layout)

    return fig
